package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for parent-child relationship methods in MemberService
 */
@ExtendWith(MockitoExtension.class)
public class MemberServiceParentChildTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Church church;
    private Member parent;
    private Member child;

    @BeforeEach
    void setUp() {
        church = new Church();
        church.setId(1L);
        church.setName("Test Church");

        parent = new Member();
        parent.setId(1L);
        parent.setFirstName("John");
        parent.setLastName("Doe");
        parent.setChurch(church);
        parent.setPhoneNumber("+233241234567");
        parent.setChildren(new HashSet<>());
        parent.setParents(new HashSet<>());

        child = new Member();
        child.setId(2L);
        child.setFirstName("Jane");
        child.setLastName("Doe");
        child.setChurch(church);
        child.setPhoneNumber("+233241234568");
        child.setChildren(new HashSet<>());
        child.setParents(new HashSet<>());
    }

    @Test
    void testAddParent_Success() {
        // Arrange
        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(memberRepository.save(any(Member.class))).thenReturn(child);

        // Act
        MemberResponse result = memberService.addParent(child.getId(), parent.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(child.getId(), result.id());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository).findById(parent.getId());
        verify(memberRepository).save(child);
        assertTrue(child.getParents().contains(parent));
    }

    @Test
    void testAddParent_ChildNotFound() {
        // Arrange
        when(memberRepository.findById(child.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.addParent(child.getId(), parent.getId(), church.getId());
        });

        assertEquals("Child member not found: " + child.getId(), exception.getMessage());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository, never()).findById(parent.getId());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testAddParent_ParentNotFound() {
        // Arrange
        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.addParent(child.getId(), parent.getId(), church.getId());
        });

        assertEquals("Parent member not found: " + parent.getId(), exception.getMessage());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository).findById(parent.getId());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testAddParent_SelfParenting() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.addParent(parent.getId(), parent.getId(), church.getId());
        });

        assertEquals("A member cannot be their own parent", exception.getMessage());
        verify(memberRepository, never()).findById(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testAddParent_DifferentChurch_Child() {
        // Arrange
        Church differentChurch = new Church();
        differentChurch.setId(2L);
        differentChurch.setName("Different Church");
        child.setChurch(differentChurch);

        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.addParent(child.getId(), parent.getId(), church.getId());
        });

        assertEquals("Child member does not belong to your church", exception.getMessage());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository).findById(parent.getId());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testAddParent_DifferentChurch_Parent() {
        // Arrange
        Church differentChurch = new Church();
        differentChurch.setId(2L);
        differentChurch.setName("Different Church");
        parent.setChurch(differentChurch);

        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.addParent(child.getId(), parent.getId(), church.getId());
        });

        assertEquals("Parent member does not belong to your church", exception.getMessage());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository).findById(parent.getId());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testRemoveParent_Success() {
        // Arrange
        child.getParents().add(parent);
        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(memberRepository.save(any(Member.class))).thenReturn(child);

        // Act
        MemberResponse result = memberService.removeParent(child.getId(), parent.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(child.getId(), result.id());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository).findById(parent.getId());
        verify(memberRepository).save(child);
        assertFalse(child.getParents().contains(parent));
    }

    @Test
    void testRemoveParent_ChildNotFound() {
        // Arrange
        when(memberRepository.findById(child.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.removeParent(child.getId(), parent.getId(), church.getId());
        });

        assertEquals("Child member not found: " + child.getId(), exception.getMessage());
        verify(memberRepository).findById(child.getId());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testGetParents_Success() {
        // Arrange
        child.getParents().add(parent);
        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));

        // Act
        List<MemberResponse> result = memberService.getParents(child.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(parent.getId(), result.get(0).id());
        assertEquals("John Doe", result.get(0).firstName() + " " + result.get(0).lastName());
        verify(memberRepository).findById(child.getId());
    }

    @Test
    void testGetParents_NoParents() {
        // Arrange
        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));

        // Act
        List<MemberResponse> result = memberService.getParents(child.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(memberRepository).findById(child.getId());
    }

    @Test
    void testGetChildren_Success() {
        // Arrange
        parent.getChildren().add(child);
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // Act
        List<MemberResponse> result = memberService.getChildren(parent.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(child.getId(), result.get(0).id());
        assertEquals("Jane Doe", result.get(0).firstName() + " " + result.get(0).lastName());
        verify(memberRepository).findById(parent.getId());
    }

    @Test
    void testGetChildren_NoChildren() {
        // Arrange
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // Act
        List<MemberResponse> result = memberService.getChildren(parent.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(memberRepository).findById(parent.getId());
    }

    @Test
    void testAddParent_MultipleParents() {
        // Arrange - Add second parent
        Member parent2 = new Member();
        parent2.setId(3L);
        parent2.setFirstName("Mary");
        parent2.setLastName("Doe");
        parent2.setChurch(church);
        parent2.setPhoneNumber("+233241234569");
        parent2.setChildren(new HashSet<>());
        parent2.setParents(new HashSet<>());

        // Add first parent
        child.getParents().add(parent);

        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent2.getId())).thenReturn(Optional.of(parent2));
        when(memberRepository.save(any(Member.class))).thenReturn(child);

        // Act - Add second parent
        MemberResponse result = memberService.addParent(child.getId(), parent2.getId(), church.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, child.getParents().size());
        assertTrue(child.getParents().contains(parent));
        assertTrue(child.getParents().contains(parent2));
    }

    @Test
    void testAddChild_ViaAddParent() {
        // Arrange - Adding a child is done by adding the current member as parent to the child
        when(memberRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(memberRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(memberRepository.save(any(Member.class))).thenReturn(child);

        // Act - Add parent to child (which makes parent have child in bidirectional relationship)
        memberService.addParent(child.getId(), parent.getId(), church.getId());

        // Assert
        assertTrue(child.getParents().contains(parent));
        // Note: bidirectional relationship means parent.getChildren() should also contain child
        // but this depends on JPA cascade settings
    }
}
