package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.ConfidentialNoteRequest;
import com.reuben.pastcare_spring.dtos.ConfidentialNoteResponse;
import com.reuben.pastcare_spring.mapper.ConfidentialNoteMapper;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing confidential notes about members.
 * Handles role-based access control for sensitive pastoral care information.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@Service
@Transactional
public class ConfidentialNoteService {

    @Autowired
    private ConfidentialNoteRepository confidentialNoteRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunicationLogRepository communicationLogRepository;

    /**
     * Create a new confidential note for a member.
     */
    public ConfidentialNoteResponse createConfidentialNote(Long churchId, ConfidentialNoteRequest request, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(request.memberId(), church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.memberId()));

        User createdBy = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        ConfidentialNote note = new ConfidentialNote();
        note.setChurch(church);
        note.setMember(member);
        note.setCategory(request.category());
        note.setSubject(request.subject());
        note.setContent(request.content());
        note.setCreatedBy(createdBy);
        note.setPriority(request.priority() != null ? request.priority() : CommunicationPriority.NORMAL);
        note.setRequiresFollowUp(request.requiresFollowUp() != null ? request.requiresFollowUp() : false);
        note.setFollowUpDate(request.followUpDate());
        note.setFollowUpStatus(request.followUpStatus());
        note.setMinimumRoleRequired(request.minimumRoleRequired());
        note.setIsArchived(false);
        note.setTags(request.tags());

        if (request.relatedCommunicationId() != null) {
            CommunicationLog relatedCommunication = communicationLogRepository.findById(request.relatedCommunicationId())
                .orElse(null);
            note.setRelatedCommunication(relatedCommunication);
        }

        ConfidentialNote savedNote = confidentialNoteRepository.save(note);
        return ConfidentialNoteMapper.toResponse(savedNote);
    }

    /**
     * Update an existing confidential note.
     */
    public ConfidentialNoteResponse updateConfidentialNote(Long churchId, Long noteId, ConfidentialNoteRequest request, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        ConfidentialNote note = confidentialNoteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Confidential note not found with id: " + noteId));

        if (!note.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Confidential note does not belong to your church");
        }

        User modifiedBy = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        note.setCategory(request.category());
        note.setSubject(request.subject());
        note.setContent(request.content());
        note.setPriority(request.priority() != null ? request.priority() : CommunicationPriority.NORMAL);
        note.setRequiresFollowUp(request.requiresFollowUp() != null ? request.requiresFollowUp() : false);
        note.setFollowUpDate(request.followUpDate());
        note.setFollowUpStatus(request.followUpStatus());
        note.setMinimumRoleRequired(request.minimumRoleRequired());
        note.setTags(request.tags());
        note.setLastModifiedAt(LocalDateTime.now());
        note.setLastModifiedBy(modifiedBy);

        if (request.relatedCommunicationId() != null) {
            CommunicationLog relatedCommunication = communicationLogRepository.findById(request.relatedCommunicationId())
                .orElse(null);
            note.setRelatedCommunication(relatedCommunication);
        }

        ConfidentialNote updatedNote = confidentialNoteRepository.save(note);
        return ConfidentialNoteMapper.toResponse(updatedNote);
    }

    /**
     * Get all confidential notes for a member (non-archived).
     */
    public List<ConfidentialNoteResponse> getMemberConfidentialNotes(Long churchId, Long memberId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        return confidentialNoteRepository.findByMember(member).stream()
            .map(ConfidentialNoteMapper::toResponse)
            .toList();
    }

    /**
     * Get all confidential notes for a church with pagination.
     */
    public Page<ConfidentialNoteResponse> getChurchConfidentialNotes(Long churchId, Boolean includeArchived, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Boolean isArchived = includeArchived != null && includeArchived ? null : false;

        return confidentialNoteRepository.findByChurchAndIsArchived(church, isArchived != null ? isArchived : false, pageable)
            .map(ConfidentialNoteMapper::toResponse);
    }

    /**
     * Get confidential notes by category.
     */
    public Page<ConfidentialNoteResponse> getConfidentialNotesByCategory(Long churchId, ConfidentialNoteCategory category, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return confidentialNoteRepository.findByChurchAndCategoryAndIsArchived(church, category, false, pageable)
            .map(ConfidentialNoteMapper::toResponse);
    }

    /**
     * Get confidential notes requiring follow-up.
     */
    public List<ConfidentialNoteResponse> getFollowUpRequired(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return confidentialNoteRepository.findByChurchAndRequiresFollowUp(church).stream()
            .map(ConfidentialNoteMapper::toResponse)
            .toList();
    }

    /**
     * Get overdue follow-ups for confidential notes.
     */
    public List<ConfidentialNoteResponse> getOverdueFollowUps(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return confidentialNoteRepository.findOverdueFollowUps(church, LocalDateTime.now()).stream()
            .map(ConfidentialNoteMapper::toResponse)
            .toList();
    }

    /**
     * Get high priority confidential notes.
     */
    public List<ConfidentialNoteResponse> getHighPriorityNotes(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return confidentialNoteRepository.findHighPriorityNotes(church).stream()
            .map(ConfidentialNoteMapper::toResponse)
            .toList();
    }

    /**
     * Search confidential notes by subject or tags.
     */
    public Page<ConfidentialNoteResponse> searchNotes(Long churchId, String search, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return confidentialNoteRepository.searchNotes(church, search, pageable)
            .map(ConfidentialNoteMapper::toResponse);
    }

    /**
     * Archive a confidential note.
     */
    public ConfidentialNoteResponse archiveNote(Long churchId, Long noteId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        ConfidentialNote note = confidentialNoteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Confidential note not found with id: " + noteId));

        if (!note.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Confidential note does not belong to your church");
        }

        note.setIsArchived(true);
        ConfidentialNote archivedNote = confidentialNoteRepository.save(note);
        return ConfidentialNoteMapper.toResponse(archivedNote);
    }

    /**
     * Unarchive a confidential note.
     */
    public ConfidentialNoteResponse unarchiveNote(Long churchId, Long noteId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        ConfidentialNote note = confidentialNoteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Confidential note not found with id: " + noteId));

        if (!note.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Confidential note does not belong to your church");
        }

        note.setIsArchived(false);
        ConfidentialNote unarchivedNote = confidentialNoteRepository.save(note);
        return ConfidentialNoteMapper.toResponse(unarchivedNote);
    }

    /**
     * Update follow-up status.
     */
    public ConfidentialNoteResponse updateFollowUpStatus(Long churchId, Long noteId, FollowUpStatus status) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        ConfidentialNote note = confidentialNoteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Confidential note not found with id: " + noteId));

        if (!note.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Confidential note does not belong to your church");
        }

        note.setFollowUpStatus(status);
        ConfidentialNote updatedNote = confidentialNoteRepository.save(note);
        return ConfidentialNoteMapper.toResponse(updatedNote);
    }

    /**
     * Delete a confidential note (hard delete).
     */
    public void deleteConfidentialNote(Long churchId, Long noteId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        ConfidentialNote note = confidentialNoteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Confidential note not found with id: " + noteId));

        if (!note.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Confidential note does not belong to your church");
        }

        confidentialNoteRepository.delete(note);
    }

    /**
     * Get a single confidential note by ID.
     */
    public ConfidentialNoteResponse getConfidentialNoteById(Long churchId, Long noteId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        ConfidentialNote note = confidentialNoteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("Confidential note not found with id: " + noteId));

        if (!note.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Confidential note does not belong to your church");
        }

        return ConfidentialNoteMapper.toResponse(note);
    }
}
