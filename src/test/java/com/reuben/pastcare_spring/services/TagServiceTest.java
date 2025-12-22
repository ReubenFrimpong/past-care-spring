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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tag Service Tests")
class TagServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChurchRepository churchRepository;

    @InjectMocks
    private MemberService memberService;

    private Church church;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        church = new Church();
        church.setId(1L);
        church.setName("Test Church");
        church.setPhoneNumber("+233241234567");

        member1 = new Member();
        member1.setId(1L);
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setPhoneNumber("+233241111111");
        member1.setSex("Male");
        member1.setMaritalStatus("single");
        member1.setChurch(church);
        member1.setTags(new HashSet<>(Arrays.asList("youth", "choir")));

        member2 = new Member();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Smith");
        member2.setPhoneNumber("+233242222222");
        member2.setSex("Female");
        member2.setMaritalStatus("married");
        member2.setChurch(church);
        member2.setTags(new HashSet<>(Arrays.asList("choir", "usher")));
    }

    @Test
    @DisplayName("Should add tags to a member")
    void testAddTags() {
        Set<String> tagsToAdd = new HashSet<>(Arrays.asList("leader", "teacher"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenReturn(member1);

        MemberResponse response = memberService.addTags(1L, tagsToAdd);

        assertNotNull(response);
        assertTrue(member1.getTags().contains("leader"));
        assertTrue(member1.getTags().contains("teacher"));
        assertTrue(member1.getTags().contains("youth")); // Existing tag preserved
        assertTrue(member1.getTags().contains("choir")); // Existing tag preserved
        verify(memberRepository).save(member1);
    }

    @Test
    @DisplayName("Should normalize tags to lowercase when adding")
    void testAddTagsNormalization() {
        Set<String> tagsToAdd = new HashSet<>(Arrays.asList("LEADER", "Teacher"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenReturn(member1);

        memberService.addTags(1L, tagsToAdd);

        assertTrue(member1.getTags().contains("leader"));
        assertTrue(member1.getTags().contains("teacher"));
        assertFalse(member1.getTags().contains("LEADER"));
        assertFalse(member1.getTags().contains("Teacher"));
    }

    @Test
    @DisplayName("Should reject invalid tag format when adding")
    void testAddTagsValidation() {
        Set<String> invalidTags = new HashSet<>(Arrays.asList("invalid tag", "tag@123"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.addTags(1L, invalidTags);
        });

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should reject tag longer than 50 characters")
    void testAddTagsTooLong() {
        Set<String> longTag = new HashSet<>(Arrays.asList("a".repeat(51)));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.addTags(1L, longTag);
        });

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should remove tags from a member")
    void testRemoveTags() {
        Set<String> tagsToRemove = new HashSet<>(Arrays.asList("choir"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenReturn(member1);

        MemberResponse response = memberService.removeTags(1L, tagsToRemove);

        assertNotNull(response);
        assertFalse(member1.getTags().contains("choir"));
        assertTrue(member1.getTags().contains("youth")); // Other tag preserved
        verify(memberRepository).save(member1);
    }

    @Test
    @DisplayName("Should handle removing non-existent tags gracefully")
    void testRemoveNonExistentTags() {
        Set<String> tagsToRemove = new HashSet<>(Arrays.asList("nonexistent"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenReturn(member1);

        MemberResponse response = memberService.removeTags(1L, tagsToRemove);

        assertNotNull(response);
        assertEquals(2, member1.getTags().size()); // Original tags unchanged
        verify(memberRepository).save(member1);
    }

    @Test
    @DisplayName("Should replace all tags for a member")
    void testSetTags() {
        Set<String> newTags = new HashSet<>(Arrays.asList("new-tag1", "new-tag2"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenReturn(member1);

        MemberResponse response = memberService.setTags(1L, newTags);

        assertNotNull(response);
        assertEquals(2, member1.getTags().size());
        assertTrue(member1.getTags().contains("new-tag1"));
        assertTrue(member1.getTags().contains("new-tag2"));
        assertFalse(member1.getTags().contains("youth"));
        assertFalse(member1.getTags().contains("choir"));
        verify(memberRepository).save(member1);
    }

    @Test
    @DisplayName("Should clear all tags when setting empty set")
    void testSetTagsEmpty() {
        Set<String> emptyTags = new HashSet<>();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenReturn(member1);

        MemberResponse response = memberService.setTags(1L, emptyTags);

        assertNotNull(response);
        assertTrue(member1.getTags().isEmpty());
        verify(memberRepository).save(member1);
    }

    @Test
    @DisplayName("Should get all unique tags with counts")
    void testGetAllTags() {
        List<Member> members = Arrays.asList(member1, member2);
        Page<Member> memberPage = new PageImpl<>(members);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurch(eq(church), any(Pageable.class))).thenReturn(memberPage);

        Map<String, Long> tagCounts = memberService.getAllTags(1L);

        assertNotNull(tagCounts);
        assertEquals(3, tagCounts.size());
        assertEquals(2L, tagCounts.get("choir")); // Both members have choir
        assertEquals(1L, tagCounts.get("youth")); // Only member1 has youth
        assertEquals(1L, tagCounts.get("usher")); // Only member2 has usher
    }

    @Test
    @DisplayName("Should return empty map when no tags exist")
    void testGetAllTagsEmpty() {
        member1.setTags(new HashSet<>());
        member2.setTags(new HashSet<>());
        List<Member> members = Arrays.asList(member1, member2);
        Page<Member> memberPage = new PageImpl<>(members);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurch(eq(church), any(Pageable.class))).thenReturn(memberPage);

        Map<String, Long> tagCounts = memberService.getAllTags(1L);

        assertNotNull(tagCounts);
        assertTrue(tagCounts.isEmpty());
    }

    @Test
    @DisplayName("Should get members by tag")
    void testGetMembersByTag() {
        List<Member> choirMembers = Arrays.asList(member1, member2);
        Page<Member> memberPage = new PageImpl<>(choirMembers);
        Pageable pageable = PageRequest.of(0, 20);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurchAndTagsContaining(church, "choir", pageable))
                .thenReturn(memberPage);

        Page<MemberResponse> response = memberService.getMembersByTag(1L, "choir", pageable);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
    }

    @Test
    @DisplayName("Should normalize tag when searching by tag")
    void testGetMembersByTagNormalization() {
        List<Member> choirMembers = Arrays.asList(member1, member2);
        Page<Member> memberPage = new PageImpl<>(choirMembers);
        Pageable pageable = PageRequest.of(0, 20);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(memberRepository.findByChurchAndTagsContaining(church, "choir", pageable))
                .thenReturn(memberPage);

        // Search with uppercase - should be normalized to lowercase
        Page<MemberResponse> response = memberService.getMembersByTag(1L, "CHOIR", pageable);

        assertNotNull(response);
        verify(memberRepository).findByChurchAndTagsContaining(church, "choir", pageable);
    }

    @Test
    @DisplayName("Should throw exception when member not found for tag operations")
    void testTagOperationsMemberNotFound() {
        Set<String> tags = new HashSet<>(Arrays.asList("test"));

        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.addTags(999L, tags);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.removeTags(999L, tags);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.setTags(999L, tags);
        });
    }

    @Test
    @DisplayName("Should throw exception when church not found for tag queries")
    void testTagQueriesChurchNotFound() {
        when(churchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.getAllTags(999L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.getMembersByTag(999L, "test", PageRequest.of(0, 20));
        });
    }
}
