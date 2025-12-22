package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MemberService tag management methods.
 * Tests tag normalization, validation, and CRUD operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Member Service Tag Management Tests")
class MemberServiceTagTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChurchRepository churchRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private Church church;

    @BeforeEach
    void setUp() {
        church = new Church();
        church.setId(1L);
        church.setName("Test Church");
        church.setEmail("test@church.com");
        church.setPhoneNumber("+233241234567");

        member = new Member();
        member.setId(1L);
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setPhoneNumber("+233241234567");
        member.setSex("male");
        member.setMaritalStatus("single");
        member.setChurch(church);
        member.setTags(new HashSet<>());
    }

    // ========== Add Tags Tests ==========

    @Test
    @DisplayName("Should add tags successfully")
    void testAddTags() {
        // Given
        Set<String> tagsToAdd = Set.of("youth", "choir");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = memberService.addTags(1L, tagsToAdd);

        // Then
        assertNotNull(response);
        assertTrue(member.getTags().contains("youth"));
        assertTrue(member.getTags().contains("choir"));
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should normalize tags to lowercase when adding")
    void testAddTags_Normalization() {
        // Given
        Set<String> tagsToAdd = Set.of("YOUTH", "Choir", "LEADER");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.addTags(1L, tagsToAdd);

        // Then
        assertTrue(member.getTags().contains("youth"));
        assertTrue(member.getTags().contains("choir"));
        assertTrue(member.getTags().contains("leader"));
        assertFalse(member.getTags().contains("YOUTH"));
    }

    @Test
    @DisplayName("Should trim whitespace from tags when adding")
    void testAddTags_Trimming() {
        // Given
        Set<String> tagsToAdd = Set.of("  youth  ", "  choir  ");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.addTags(1L, tagsToAdd);

        // Then
        assertTrue(member.getTags().contains("youth"));
        assertTrue(member.getTags().contains("choir"));
    }

    @Test
    @DisplayName("Should throw exception for invalid tag format")
    void testAddTags_InvalidFormat() {
        // Given
        Set<String> invalidTags = Set.of("invalid tag!", "tag@123");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.addTags(1L, invalidTags)
        );
        assertTrue(exception.getMessage().contains("Invalid tag format"));
    }

    @Test
    @DisplayName("Should throw exception for tag exceeding 50 characters")
    void testAddTags_TooLong() {
        // Given
        String longTag = "a".repeat(51);
        Set<String> invalidTags = Set.of(longTag);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.addTags(1L, invalidTags)
        );
        assertTrue(exception.getMessage().contains("Invalid tag format"));
    }

    @Test
    @DisplayName("Should accept valid tag formats")
    void testAddTags_ValidFormats() {
        // Given
        Set<String> validTags = Set.of("youth", "youth-group", "youth_ministry", "event-2024");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.addTags(1L, validTags);

        // Then
        assertEquals(4, member.getTags().size());
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void testAddTags_MemberNotFound() {
        // Given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.addTags(999L, Set.of("tag"))
        );
        assertEquals("Member not found", exception.getMessage());
    }

    // ========== Remove Tags Tests ==========

    @Test
    @DisplayName("Should remove tags successfully")
    void testRemoveTags() {
        // Given
        member.getTags().addAll(Set.of("youth", "choir", "leader"));
        Set<String> tagsToRemove = Set.of("choir");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = memberService.removeTags(1L, tagsToRemove);

        // Then
        assertNotNull(response);
        assertTrue(member.getTags().contains("youth"));
        assertTrue(member.getTags().contains("leader"));
        assertFalse(member.getTags().contains("choir"));
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should normalize tags when removing")
    void testRemoveTags_Normalization() {
        // Given
        member.getTags().addAll(Set.of("youth", "choir"));
        Set<String> tagsToRemove = Set.of("YOUTH");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.removeTags(1L, tagsToRemove);

        // Then
        assertFalse(member.getTags().contains("youth"));
        assertTrue(member.getTags().contains("choir"));
    }

    // ========== Set Tags Tests ==========

    @Test
    @DisplayName("Should replace all tags")
    void testSetTags() {
        // Given
        member.getTags().addAll(Set.of("youth", "choir"));
        Set<String> newTags = Set.of("leader", "worship");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = memberService.setTags(1L, newTags);

        // Then
        assertNotNull(response);
        assertEquals(2, member.getTags().size());
        assertTrue(member.getTags().contains("leader"));
        assertTrue(member.getTags().contains("worship"));
        assertFalse(member.getTags().contains("youth"));
        assertFalse(member.getTags().contains("choir"));
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should clear all tags when empty set provided")
    void testSetTags_EmptySet() {
        // Given
        member.getTags().addAll(Set.of("youth", "choir"));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.setTags(1L, new HashSet<>());

        // Then
        assertEquals(0, member.getTags().size());
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should clear all tags when null provided")
    void testSetTags_Null() {
        // Given
        member.getTags().addAll(Set.of("youth", "choir"));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.setTags(1L, null);

        // Then
        assertEquals(0, member.getTags().size());
        verify(memberRepository).save(member);
    }

    // ========== Get All Tags Tests ==========

    @Test
    @DisplayName("Should get all tags with counts")
    void testGetAllTags() {
        // Given
        Member member1 = new Member();
        member1.setTags(new HashSet<>(Set.of("youth", "choir")));

        Member member2 = new Member();
        member2.setTags(new HashSet<>(Set.of("youth", "leader")));

        Member member3 = new Member();
        member3.setTags(new HashSet<>(Set.of("choir")));

        List<Member> members = List.of(member1, member2, member3);
        Page<Member> membersPage = new PageImpl<>(members);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurch(eq(church), any(Pageable.class))).thenReturn(membersPage);

        // When
        Map<String, Long> tagCounts = memberService.getAllTags(1L);

        // Then
        assertEquals(3, tagCounts.size());
        assertEquals(2L, tagCounts.get("youth"));
        assertEquals(2L, tagCounts.get("choir"));
        assertEquals(1L, tagCounts.get("leader"));
    }

    @Test
    @DisplayName("Should return empty map when no tags exist")
    void testGetAllTags_NoTags() {
        // Given
        List<Member> members = List.of(member); // member has no tags
        Page<Member> membersPage = new PageImpl<>(members);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurch(eq(church), any(Pageable.class))).thenReturn(membersPage);

        // When
        Map<String, Long> tagCounts = memberService.getAllTags(1L);

        // Then
        assertEquals(0, tagCounts.size());
    }

    @Test
    @DisplayName("Should throw exception when church not found for getAllTags")
    void testGetAllTags_ChurchNotFound() {
        // Given
        when(churchRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.getAllTags(999L)
        );
        assertEquals("Invalid church ID", exception.getMessage());
    }

    // ========== Get Members By Tag Tests ==========

    @Test
    @DisplayName("Should find members by tag")
    void testGetMembersByTag() {
        // Given
        member.getTags().add("youth");
        List<Member> members = List.of(member);
        Page<Member> membersPage = new PageImpl<>(members);
        Pageable pageable = PageRequest.of(0, 20);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurchAndTagsContaining(church, "youth", pageable))
            .thenReturn(membersPage);

        // When
        Page<MemberResponse> result = memberService.getMembersByTag(1L, "youth", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).firstName());
        verify(memberRepository).findByChurchAndTagsContaining(church, "youth", pageable);
    }

    @Test
    @DisplayName("Should normalize tag when searching")
    void testGetMembersByTag_Normalization() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurchAndTagsContaining(any(), eq("youth"), any()))
            .thenReturn(Page.empty());

        // When
        memberService.getMembersByTag(1L, "  YOUTH  ", pageable);

        // Then
        verify(memberRepository).findByChurchAndTagsContaining(church, "youth", pageable);
    }

    @Test
    @DisplayName("Should throw exception when church not found for getMembersByTag")
    void testGetMembersByTag_ChurchNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(churchRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.getMembersByTag(999L, "tag", pageable)
        );
        assertEquals("Invalid church ID", exception.getMessage());
    }
}
