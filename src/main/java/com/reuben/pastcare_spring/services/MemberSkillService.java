package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.MemberSkillRequest;
import com.reuben.pastcare_spring.enums.ProficiencyLevel;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.MemberSkill;
import com.reuben.pastcare_spring.models.Skill;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.MemberSkillRepository;
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
public class MemberSkillService {

    private final MemberSkillRepository memberSkillRepository;
    private final MemberRepository memberRepository;
    private final SkillRepository skillRepository;

    /**
     * Assign a skill to a member
     */
    public MemberSkill assignSkillToMember(Long churchId, MemberSkillRequest request) {
        // Verify member belongs to church
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + request.getMemberId()));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        // Verify skill belongs to church
        Skill skill = skillRepository.findById(request.getSkillId())
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + request.getSkillId()));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        // Check if member already has this skill
        memberSkillRepository.findByMemberIdAndSkillId(request.getMemberId(), request.getSkillId())
            .ifPresent(ms -> {
                throw new IllegalArgumentException("Member already has this skill assigned");
            });

        MemberSkill memberSkill = new MemberSkill();
        memberSkill.setMember(member);
        memberSkill.setSkill(skill);
        memberSkill.setProficiencyLevel(request.getProficiencyLevel());
        memberSkill.setWillingToServe(request.getWillingToServe());
        memberSkill.setCurrentlyServing(request.getCurrentlyServing());
        memberSkill.setYearsOfExperience(request.getYearsOfExperience());
        memberSkill.setNotes(request.getNotes());
        memberSkill.setAcquiredDate(request.getAcquiredDate());
        memberSkill.setLastVerifiedDate(request.getLastVerifiedDate());
        memberSkill.setChurchId(churchId);

        MemberSkill savedMemberSkill = memberSkillRepository.save(memberSkill);
        log.info("Assigned skill {} to member {} (ID: {})", skill.getName(), member.getFirstName(), savedMemberSkill.getId());
        return savedMemberSkill;
    }

    /**
     * Update member skill information
     */
    public MemberSkill updateMemberSkill(Long churchId, Long memberSkillId, MemberSkillRequest request) {
        MemberSkill memberSkill = memberSkillRepository.findById(memberSkillId)
            .orElseThrow(() -> new IllegalArgumentException("Member skill not found with ID: " + memberSkillId));

        if (!memberSkill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member skill");
        }

        memberSkill.setProficiencyLevel(request.getProficiencyLevel());
        memberSkill.setWillingToServe(request.getWillingToServe());
        memberSkill.setCurrentlyServing(request.getCurrentlyServing());
        memberSkill.setYearsOfExperience(request.getYearsOfExperience());
        memberSkill.setNotes(request.getNotes());
        memberSkill.setAcquiredDate(request.getAcquiredDate());
        memberSkill.setLastVerifiedDate(request.getLastVerifiedDate());

        MemberSkill updatedMemberSkill = memberSkillRepository.save(memberSkill);
        log.info("Updated member skill (ID: {})", updatedMemberSkill.getId());
        return updatedMemberSkill;
    }

    /**
     * Get all skills for a member
     */
    @Transactional(readOnly = true)
    public List<MemberSkill> getMemberSkills(Long churchId, Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        return memberSkillRepository.findByMemberId(memberId);
    }

    /**
     * Get all members with a specific skill
     */
    @Transactional(readOnly = true)
    public List<MemberSkill> getMembersWithSkill(Long churchId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));

        if (!skill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to skill");
        }

        return memberSkillRepository.findBySkillId(skillId);
    }

    /**
     * Get members willing to serve with a specific skill
     */
    @Transactional(readOnly = true)
    public List<MemberSkill> getMembersWillingToServe(Long churchId, Long skillId) {
        return memberSkillRepository.findMembersWillingToServeBySkill(churchId, skillId);
    }

    /**
     * Get members by skill and proficiency level
     */
    @Transactional(readOnly = true)
    public List<MemberSkill> getMembersBySkillAndProficiency(Long churchId, Long skillId, List<ProficiencyLevel> proficiencyLevels) {
        return memberSkillRepository.findMembersBySkillAndProficiency(churchId, skillId, proficiencyLevels);
    }

    /**
     * Get skills a member is currently serving with
     */
    @Transactional(readOnly = true)
    public List<MemberSkill> getCurrentlyServingSkills(Long churchId, Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        return memberSkillRepository.findCurrentlyServingSkills(memberId);
    }

    /**
     * Remove a skill from a member
     */
    public void removeMemberSkill(Long churchId, Long memberSkillId) {
        MemberSkill memberSkill = memberSkillRepository.findById(memberSkillId)
            .orElseThrow(() -> new IllegalArgumentException("Member skill not found with ID: " + memberSkillId));

        if (!memberSkill.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member skill");
        }

        memberSkillRepository.delete(memberSkill);
        log.info("Removed member skill (ID: {})", memberSkillId);
    }

    /**
     * Get member skill count
     */
    @Transactional(readOnly = true)
    public Long getMemberSkillCount(Long churchId, Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        return memberSkillRepository.countMemberSkills(memberId);
    }
}
