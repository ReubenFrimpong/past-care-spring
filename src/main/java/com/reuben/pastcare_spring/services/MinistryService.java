package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.MinistryRequest;
import com.reuben.pastcare_spring.enums.MinistryStatus;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.Ministry;
import com.reuben.pastcare_spring.models.Skill;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.MinistryRepository;
import com.reuben.pastcare_spring.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MinistryService {

    private final MinistryRepository ministryRepository;
    private final MemberRepository memberRepository;
    private final SkillRepository skillRepository;

    /**
     * Create a new ministry
     */
    public Ministry createMinistry(Long churchId, MinistryRequest request) {
        // Check if ministry with same name already exists
        ministryRepository.findByNameAndChurchId(request.getName(), churchId)
            .ifPresent(m -> {
                throw new IllegalArgumentException("Ministry with name '" + request.getName() + "' already exists");
            });

        Ministry ministry = new Ministry();
        ministry.setName(request.getName());
        ministry.setDescription(request.getDescription());
        ministry.setStatus(request.getStatus());
        ministry.setMeetingSchedule(request.getMeetingSchedule());
        ministry.setContactEmail(request.getContactEmail());
        ministry.setContactPhone(request.getContactPhone());
        ministry.setChurchId(churchId);

        // Set leader if provided
        if (request.getLeaderId() != null) {
            Member leader = memberRepository.findById(request.getLeaderId())
                .orElseThrow(() -> new IllegalArgumentException("Leader not found with ID: " + request.getLeaderId()));

            if (!leader.getChurch().getId().equals(churchId)) {
                throw new IllegalArgumentException("Unauthorized access to member");
            }
            ministry.setLeader(leader);
        }

        // Set required skills if provided
        if (request.getRequiredSkillIds() != null && !request.getRequiredSkillIds().isEmpty()) {
            Set<Skill> skills = request.getRequiredSkillIds().stream()
                .map(skillId -> skillRepository.findById(skillId)
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId)))
                .collect(Collectors.toSet());
            ministry.setRequiredSkills(skills);
        }

        // Set members if provided
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<Member> members = request.getMemberIds().stream()
                .map(memberId -> memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId)))
                .collect(Collectors.toSet());
            ministry.setMembers(members);
        }

        Ministry savedMinistry = ministryRepository.save(ministry);
        log.info("Created ministry: {} (ID: {}) for church: {}", savedMinistry.getName(), savedMinistry.getId(), churchId);
        return savedMinistry;
    }

    /**
     * Update an existing ministry
     */
    public Ministry updateMinistry(Long churchId, Long ministryId, MinistryRequest request) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        // Check if updating name would cause conflict
        if (!ministry.getName().equals(request.getName())) {
            ministryRepository.findByNameAndChurchId(request.getName(), churchId)
                .ifPresent(m -> {
                    if (!m.getId().equals(ministryId)) {
                        throw new IllegalArgumentException("Ministry with name '" + request.getName() + "' already exists");
                    }
                });
        }

        ministry.setName(request.getName());
        ministry.setDescription(request.getDescription());
        ministry.setStatus(request.getStatus());
        ministry.setMeetingSchedule(request.getMeetingSchedule());
        ministry.setContactEmail(request.getContactEmail());
        ministry.setContactPhone(request.getContactPhone());

        // Update leader if provided
        if (request.getLeaderId() != null) {
            Member leader = memberRepository.findById(request.getLeaderId())
                .orElseThrow(() -> new IllegalArgumentException("Leader not found with ID: " + request.getLeaderId()));

            if (!leader.getChurch().getId().equals(churchId)) {
                throw new IllegalArgumentException("Unauthorized access to member");
            }
            ministry.setLeader(leader);
        } else {
            ministry.setLeader(null);
        }

        // Update required skills if provided
        if (request.getRequiredSkillIds() != null) {
            Set<Skill> skills = request.getRequiredSkillIds().stream()
                .map(skillId -> skillRepository.findById(skillId)
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId)))
                .collect(Collectors.toSet());
            ministry.setRequiredSkills(skills);
        }

        // Update members if provided
        if (request.getMemberIds() != null) {
            Set<Member> members = request.getMemberIds().stream()
                .map(memberId -> memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId)))
                .collect(Collectors.toSet());
            ministry.setMembers(members);
        }

        Ministry updatedMinistry = ministryRepository.save(ministry);
        log.info("Updated ministry: {} (ID: {})", updatedMinistry.getName(), updatedMinistry.getId());
        return updatedMinistry;
    }

    /**
     * Get ministry by ID
     */
    @Transactional(readOnly = true)
    public Ministry getMinistryById(Long churchId, Long ministryId) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        return ministry;
    }

    /**
     * Get all ministries for current church
     */
    @Transactional(readOnly = true)
    public List<Ministry> getAllMinistries(Long churchId) {
        return ministryRepository.findByChurchId(churchId);
    }

    /**
     * Get ministries by status
     */
    @Transactional(readOnly = true)
    public List<Ministry> getMinistriesByStatus(Long churchId, MinistryStatus status) {
        return ministryRepository.findByChurchIdAndStatus(churchId, status);
    }

    /**
     * Search ministries by name
     */
    @Transactional(readOnly = true)
    public List<Ministry> searchMinistries(Long churchId, String searchTerm) {
        return ministryRepository.searchMinistries(churchId, searchTerm);
    }

    /**
     * Get ministries led by a member
     */
    @Transactional(readOnly = true)
    public List<Ministry> getMinistriesByLeader(Long churchId, Long leaderId) {
        Member leader = memberRepository.findById(leaderId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + leaderId));

        if (!leader.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        return ministryRepository.findByLeaderId(leaderId);
    }

    /**
     * Get ministries a member is assigned to
     */
    @Transactional(readOnly = true)
    public List<Ministry> getMinistriesByMember(Long churchId, Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        return ministryRepository.findMinistriesByMemberId(memberId);
    }

    /**
     * Add a member to a ministry
     */
    public Ministry addMemberToMinistry(Long churchId, Long ministryId, Long memberId) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        ministry.getMembers().add(member);
        Ministry updatedMinistry = ministryRepository.save(ministry);
        log.info("Added member {} to ministry {}", member.getFirstName(), ministry.getName());
        return updatedMinistry;
    }

    /**
     * Remove a member from a ministry
     */
    public Ministry removeMemberFromMinistry(Long churchId, Long ministryId, Long memberId) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        ministry.getMembers().remove(member);
        Ministry updatedMinistry = ministryRepository.save(ministry);
        log.info("Removed member {} from ministry {}", member.getFirstName(), ministry.getName());
        return updatedMinistry;
    }

    /**
     * Add a required skill to a ministry
     */
    public Ministry addRequiredSkill(Long churchId, Long ministryId, Long skillId) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        ministry.getRequiredSkills().add(skill);
        Ministry updatedMinistry = ministryRepository.save(ministry);
        log.info("Added skill {} to ministry {}", skill.getName(), ministry.getName());
        return updatedMinistry;
    }

    /**
     * Remove a required skill from a ministry
     */
    public Ministry removeRequiredSkill(Long churchId, Long ministryId, Long skillId) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        ministry.getRequiredSkills().remove(skill);
        Ministry updatedMinistry = ministryRepository.save(ministry);
        log.info("Removed skill {} from ministry {}", skill.getName(), ministry.getName());
        return updatedMinistry;
    }

    /**
     * Delete a ministry
     */
    public void deleteMinistry(Long churchId, Long ministryId) {
        Ministry ministry = ministryRepository.findById(ministryId)
            .orElseThrow(() -> new IllegalArgumentException("Ministry not found with ID: " + ministryId));

        if (!ministry.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to ministry");
        }

        ministryRepository.delete(ministry);
        log.info("Deleted ministry: {} (ID: {})", ministry.getName(), ministry.getId());
    }

    /**
     * Get count of active ministries
     */
    @Transactional(readOnly = true)
    public Long getActiveMinistriesCount(Long churchId) {
        return ministryRepository.countActiveMinistries(churchId);
    }
}
