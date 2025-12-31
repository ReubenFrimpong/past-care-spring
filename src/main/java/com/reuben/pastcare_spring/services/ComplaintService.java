package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dto.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.models.Complaint.ComplaintCategory;
import com.reuben.pastcare_spring.models.Complaint.ComplaintPriority;
import com.reuben.pastcare_spring.models.Complaint.ComplaintStatus;
import com.reuben.pastcare_spring.models.ComplaintActivity.ActivityType;
import com.reuben.pastcare_spring.repositories.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing complaints and feedback.
 */
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final EntityManager entityManager;

    /**
     * Enable Hibernate filter for multi-tenant data access.
     */
    private void enableChurchFilter(Long churchId) {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("churchFilter");
        filter.setParameter("churchId", churchId);
    }

    /**
     * Create a new complaint.
     */
    @Transactional
    public ComplaintDTO createComplaint(CreateComplaintRequest request, Long userId, Long churchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        Complaint complaint = new Complaint();
        complaint.setChurch(church);
        complaint.setSubmittedBy(user);
        complaint.setCategory(ComplaintCategory.valueOf(request.getCategory().toUpperCase()));
        complaint.setSubject(request.getSubject());
        complaint.setDescription(request.getDescription());
        complaint.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        complaint.setContactEmail(request.getContactEmail());
        complaint.setContactPhone(request.getContactPhone());
        complaint.setTags(request.getTags());

        // Set priority if provided, otherwise use default (MEDIUM)
        if (request.getPriority() != null && !request.getPriority().isEmpty()) {
            try {
                complaint.setPriority(ComplaintPriority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                complaint.setPriority(ComplaintPriority.MEDIUM);
            }
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Log creation activity
        createActivity(savedComplaint, user, ActivityType.CREATED,
                null, ComplaintStatus.SUBMITTED.name(),
                "Complaint submitted", true);

        return ComplaintDTO.fromEntity(savedComplaint, 1);
    }

    /**
     * Get all complaints for a church.
     */
    @Transactional(readOnly = true)
    public List<ComplaintDTO> getAllComplaints(Long churchId) {
        enableChurchFilter(churchId);
        List<Complaint> complaints = complaintRepository.findByChurchId(churchId);
        return complaints.stream()
                .map(c -> ComplaintDTO.fromEntity(c, getActivityCount(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get complaints by status.
     */
    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsByStatus(Long churchId, String statusStr) {
        enableChurchFilter(churchId);
        ComplaintStatus status = ComplaintStatus.valueOf(statusStr.toUpperCase());
        List<Complaint> complaints = complaintRepository.findByChurchIdAndStatus(churchId, status);
        return complaints.stream()
                .map(c -> ComplaintDTO.fromEntity(c, getActivityCount(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get complaints by category.
     */
    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsByCategory(Long churchId, String categoryStr) {
        enableChurchFilter(churchId);
        ComplaintCategory category = ComplaintCategory.valueOf(categoryStr.toUpperCase());
        List<Complaint> complaints = complaintRepository.findByChurchIdAndCategory(churchId, category);
        return complaints.stream()
                .map(c -> ComplaintDTO.fromEntity(c, getActivityCount(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get complaints submitted by a user.
     */
    @Transactional(readOnly = true)
    public List<ComplaintDTO> getMyComplaints(Long userId, Long churchId) {
        enableChurchFilter(churchId);
        List<Complaint> complaints = complaintRepository.findByChurchIdAndSubmittedById(churchId, userId);
        return complaints.stream()
                .map(c -> ComplaintDTO.fromEntity(c, getActivityCount(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get complaints assigned to a user.
     */
    @Transactional(readOnly = true)
    public List<ComplaintDTO> getAssignedComplaints(Long userId, Long churchId) {
        enableChurchFilter(churchId);
        List<Complaint> complaints = complaintRepository.findByChurchIdAndAssignedToId(churchId, userId);
        return complaints.stream()
                .map(c -> ComplaintDTO.fromEntity(c, getActivityCount(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get a single complaint by ID.
     */
    @Transactional(readOnly = true)
    public ComplaintDTO getComplaintById(Long id, Long churchId) {
        enableChurchFilter(churchId);
        Complaint complaint = complaintRepository.findByIdAndChurchId(id, churchId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        return ComplaintDTO.fromEntity(complaint, getActivityCount(id));
    }

    /**
     * Update complaint (admin only).
     */
    @Transactional
    public ComplaintDTO updateComplaint(Long id, UpdateComplaintRequest request, Long adminId, Long churchId) {
        enableChurchFilter(churchId);
        Complaint complaint = complaintRepository.findByIdAndChurchId(id, churchId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Track changes for activity log
        boolean hasChanges = false;

        // Update status
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            ComplaintStatus oldStatus = complaint.getStatus();
            ComplaintStatus newStatus = ComplaintStatus.valueOf(request.getStatus().toUpperCase());
            if (oldStatus != newStatus) {
                complaint.setStatus(newStatus);
                createActivity(complaint, admin, ActivityType.STATUS_CHANGED,
                        oldStatus.name(), newStatus.name(),
                        "Status changed from " + oldStatus + " to " + newStatus, true);
                hasChanges = true;
            }
        }

        // Update priority
        if (request.getPriority() != null && !request.getPriority().isEmpty()) {
            ComplaintPriority oldPriority = complaint.getPriority();
            ComplaintPriority newPriority = ComplaintPriority.valueOf(request.getPriority().toUpperCase());
            if (oldPriority != newPriority) {
                complaint.setPriority(newPriority);
                createActivity(complaint, admin, ActivityType.PRIORITY_CHANGED,
                        oldPriority.name(), newPriority.name(),
                        "Priority changed from " + oldPriority + " to " + newPriority, true);
                hasChanges = true;
            }
        }

        // Update assignment
        if (request.getAssignedToId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            String oldValue = complaint.getAssignedTo() != null ? complaint.getAssignedTo().getName() : "Unassigned";
            complaint.setAssignedTo(assignedUser);
            createActivity(complaint, admin, ActivityType.ASSIGNED,
                    oldValue, assignedUser.getName(),
                    "Assigned to " + assignedUser.getName(), true);
            hasChanges = true;
        }

        // Update admin response
        if (request.getAdminResponse() != null && !request.getAdminResponse().isEmpty()) {
            complaint.setAdminResponse(request.getAdminResponse());
            createActivity(complaint, admin, ActivityType.RESPONSE_ADDED,
                    null, null,
                    "Admin response added", true);
            hasChanges = true;
        }

        // Update internal notes (not visible to complainant)
        if (request.getInternalNotes() != null) {
            complaint.setInternalNotes(request.getInternalNotes());
            createActivity(complaint, admin, ActivityType.COMMENT_ADDED,
                    null, null,
                    "Internal notes updated", false);
            hasChanges = true;
        }

        // Update tags
        if (request.getTags() != null) {
            complaint.setTags(request.getTags());
            hasChanges = true;
        }

        if (hasChanges) {
            Complaint updated = complaintRepository.save(complaint);
            return ComplaintDTO.fromEntity(updated, getActivityCount(id));
        }

        return ComplaintDTO.fromEntity(complaint, getActivityCount(id));
    }

    /**
     * Delete a complaint (admin only).
     */
    @Transactional
    public void deleteComplaint(Long id, Long churchId) {
        enableChurchFilter(churchId);
        Complaint complaint = complaintRepository.findByIdAndChurchId(id, churchId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Delete associated activities first
        activityRepository.deleteByComplaintId(id);

        // Delete complaint
        complaintRepository.delete(complaint);
    }

    /**
     * Get complaint activities.
     */
    @Transactional(readOnly = true)
    public List<ComplaintActivityDTO> getComplaintActivities(Long complaintId, Long churchId, boolean includeInternal) {
        enableChurchFilter(churchId);

        List<ComplaintActivity> activities;
        if (includeInternal) {
            activities = activityRepository.findByComplaintIdAndChurchIdOrderByPerformedAtDesc(complaintId, churchId);
        } else {
            activities = activityRepository.findByComplaintIdAndVisibleToComplainantTrueOrderByPerformedAtDesc(complaintId);
        }

        return activities.stream()
                .map(ComplaintActivityDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get complaint statistics for a church.
     */
    @Transactional(readOnly = true)
    public ComplaintStatsDTO getComplaintStats(Long churchId) {
        enableChurchFilter(churchId);

        ComplaintStatsDTO stats = new ComplaintStatsDTO();

        // Count by status
        stats.setTotal(complaintRepository.countByChurchId(churchId));
        stats.setSubmitted(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.SUBMITTED));
        stats.setUnderReview(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.UNDER_REVIEW));
        stats.setInProgress(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.IN_PROGRESS));
        stats.setResolved(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.RESOLVED));
        stats.setPendingResponse(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.PENDING_RESPONSE));
        stats.setClosed(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.CLOSED));
        stats.setEscalated(complaintRepository.countByChurchIdAndStatus(churchId, ComplaintStatus.ESCALATED));

        // Count urgent
        stats.setUrgent(complaintRepository.countByChurchIdAndPriority(churchId, ComplaintPriority.URGENT));

        // Calculate average resolution time
        List<Complaint> resolvedComplaints = complaintRepository.findByChurchIdAndStatuses(churchId,
                List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED));

        if (!resolvedComplaints.isEmpty()) {
            double avgHours = resolvedComplaints.stream()
                    .filter(c -> c.getResolvedAt() != null)
                    .mapToLong(c -> ChronoUnit.HOURS.between(c.getSubmittedAt(), c.getResolvedAt()))
                    .average()
                    .orElse(0.0);
            stats.setAvgResolutionTimeHours(avgHours);
        } else {
            stats.setAvgResolutionTimeHours(0.0);
        }

        return stats;
    }

    /**
     * Search complaints.
     */
    @Transactional(readOnly = true)
    public List<ComplaintDTO> searchComplaints(Long churchId, String searchTerm) {
        enableChurchFilter(churchId);
        List<Complaint> complaints = complaintRepository.searchByChurchId(churchId, searchTerm);
        return complaints.stream()
                .map(c -> ComplaintDTO.fromEntity(c, getActivityCount(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to create activity log entry.
     */
    private void createActivity(Complaint complaint, User performedBy, ActivityType type,
                                String oldValue, String newValue, String description, boolean visibleToComplainant) {
        ComplaintActivity activity = new ComplaintActivity();
        activity.setChurch(complaint.getChurch());
        activity.setComplaint(complaint);
        activity.setPerformedBy(performedBy);
        activity.setActivityType(type);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activity.setDescription(description);
        activity.setVisibleToComplainant(visibleToComplainant);
        activityRepository.save(activity);
    }

    /**
     * Helper method to get activity count for a complaint.
     */
    private Integer getActivityCount(Long complaintId) {
        return activityRepository.countByComplaintId(complaintId).intValue();
    }
}
