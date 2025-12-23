package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.SkillRequest;
import com.reuben.pastcare_spring.enums.SkillCategory;
import com.reuben.pastcare_spring.models.Skill;
import com.reuben.pastcare_spring.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * Create a new skill
     */
    public Skill createSkill(Long churchId, SkillRequest request) {
        // Check if skill with same name already exists for this church
        skillRepository.findByNameAndChurchId(request.getName(), churchId)
            .ifPresent(s -> {
                throw new IllegalArgumentException("Skill with name '" + request.getName() + "' already exists");
            });

        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setCategory(request.getCategory());
        skill.setIsActive(request.getIsActive());
        skill.setChurchId(churchId);

        Skill savedSkill = skillRepository.save(skill);
        log.info("Created skill: {} (ID: {}) for church: {}", savedSkill.getName(), savedSkill.getId(), churchId);
        return savedSkill;
    }

    /**
     * Update an existing skill
     */
    public Skill updateSkill(Long churchId, Long skillId, SkillRequest request) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        // Verify ownership
        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        // Check if updating name would cause conflict
        if (!skill.getName().equals(request.getName())) {
            skillRepository.findByNameAndChurchId(request.getName(), churchId)
                .ifPresent(s -> {
                    if (!s.getId().equals(skillId)) {
                        throw new IllegalArgumentException("Skill with name '" + request.getName() + "' already exists");
                    }
                });
        }

        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setCategory(request.getCategory());
        skill.setIsActive(request.getIsActive());

        Skill updatedSkill = skillRepository.save(skill);
        log.info("Updated skill: {} (ID: {})", updatedSkill.getName(), updatedSkill.getId());
        return updatedSkill;
    }

    /**
     * Get skill by ID
     */
    @Transactional(readOnly = true)
    public Skill getSkillById(Long churchId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        return skill;
    }

    /**
     * Get all active skills for current church
     */
    @Transactional(readOnly = true)
    public List<Skill> getAllActiveSkills(Long churchId) {
        return skillRepository.findByChurchIdAndIsActive(churchId, true);
    }

    /**
     * Get all skills (active and inactive) for current church
     */
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills(Long churchId) {
        return skillRepository.findByChurchId(churchId);
    }

    /**
     * Get skills by category
     */
    @Transactional(readOnly = true)
    public List<Skill> getSkillsByCategory(Long churchId, SkillCategory category) {
        return skillRepository.findByChurchIdAndCategory(churchId, category);
    }

    /**
     * Search skills by name
     */
    @Transactional(readOnly = true)
    public List<Skill> searchSkills(Long churchId, String searchTerm) {
        return skillRepository.searchSkills(churchId, searchTerm);
    }

    /**
     * Delete a skill
     */
    public void deleteSkill(Long churchId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        skillRepository.delete(skill);
        log.info("Deleted skill: {} (ID: {})", skill.getName(), skill.getId());
    }

    /**
     * Deactivate a skill (soft delete)
     */
    public Skill deactivateSkill(Long churchId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        skill.setIsActive(false);
        Skill deactivatedSkill = skillRepository.save(skill);
        log.info("Deactivated skill: {} (ID: {})", deactivatedSkill.getName(), deactivatedSkill.getId());
        return deactivatedSkill;
    }

    /**
     * Activate a skill
     */
    public Skill activateSkill(Long churchId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        skill.setIsActive(true);
        Skill activatedSkill = skillRepository.save(skill);
        log.info("Activated skill: {} (ID: {})", activatedSkill.getName(), activatedSkill.getId());
        return activatedSkill;
    }

    /**
     * Get count of active skills
     */
    @Transactional(readOnly = true)
    public Long getActiveSkillsCount(Long churchId) {
        return skillRepository.countActiveSkills(churchId);
    }
}
