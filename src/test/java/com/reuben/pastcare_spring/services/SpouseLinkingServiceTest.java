package com.reuben.pastcare_spring.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Spouse Linking Service Tests")
class SpouseLinkingServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChurchRepository churchRepository;

    @Mock
    private ProfileCompletenessService profileCompletenessService;

    @InjectMocks
    private MemberService memberService;

    private Church church;
    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp() {
        church = new Church();
        church.setId(1L);
        church.setName("Test Church");

        member1 = new Member();
        member1.setId(1L);
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setPhoneNumber("+254700000001");
        member1.setSex("male");
        member1.setChurch(church);
        member1.setMaritalStatus("single");

        member2 = new Member();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Smith");
        member2.setPhoneNumber("+254700000002");
        member2.setSex("female");
        member2.setChurch(church);
        member2.setMaritalStatus("single");

        member3 = new Member();
        member3.setId(3L);
        member3.setFirstName("Mary");
        member3.setLastName("Johnson");
        member3.setPhoneNumber("+254700000003");
        member3.setSex("female");
        member3.setChurch(church);
        member3.setMaritalStatus("single");
    }

    @Test
    @DisplayName("Should successfully link two members as spouses")
    void linkSpouse_Success() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileCompletenessService.calculateCompleteness(any(Member.class))).thenReturn(50);

        // Act
        MemberResponse result = memberService.linkSpouse(1L, 2L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Jane Smith", member1.getSpouseName());
        assertEquals("married", member1.getMaritalStatus());
        assertEquals(member2, member1.getSpouse());
        assertEquals("John Doe", member2.getSpouseName());
        assertEquals("married", member2.getMaritalStatus());
        assertEquals(member1, member2.getSpouse());

        verify(memberRepository, times(2)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when linking member to themselves")
    void linkSpouse_SelfLink_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.linkSpouse(1L, 1L, 1L)
        );

        assertEquals("A member cannot be linked to themselves as spouse", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void linkSpouse_MemberNotFound_ThrowsException() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.linkSpouse(1L, 2L, 1L)
        );

        assertEquals("Member not found: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when spouse not found")
    void linkSpouse_SpouseNotFound_ThrowsException() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.linkSpouse(1L, 2L, 1L)
        );

        assertEquals("Spouse member not found: 2", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when member belongs to different church")
    void linkSpouse_DifferentChurch_ThrowsException() {
        // Arrange
        Church otherChurch = new Church();
        otherChurch.setId(2L);
        member1.setChurch(otherChurch);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.linkSpouse(1L, 2L, 1L)
        );

        assertEquals("Member does not belong to your church", exception.getMessage());
    }

    @Test
    @DisplayName("Should unlink existing spouse before linking new one")
    void linkSpouse_UnlinksPreviousSpouse() {
        // Arrange
        // member1 is already linked to member3
        member1.setSpouse(member3);
        member1.setSpouseName("Mary Johnson");
        member3.setSpouse(member1);
        member3.setSpouseName("John Doe");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileCompletenessService.calculateCompleteness(any(Member.class))).thenReturn(50);

        // Act
        memberService.linkSpouse(1L, 2L, 1L);

        // Assert
        assertNull(member3.getSpouse());
        assertNull(member3.getSpouseName());
        assertEquals(member2, member1.getSpouse());
        assertEquals(member1, member2.getSpouse());

        // Verify old spouse was saved (to clear the link)
        verify(memberRepository, atLeast(3)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should successfully unlink spouse")
    void unlinkSpouse_Success() {
        // Arrange
        member1.setSpouse(member2);
        member1.setSpouseName("Jane Smith");
        member2.setSpouse(member1);
        member2.setSpouseName("John Doe");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileCompletenessService.calculateCompleteness(any(Member.class))).thenReturn(40);

        // Act
        MemberResponse result = memberService.unlinkSpouse(1L, 1L);

        // Assert
        assertNotNull(result);
        assertNull(member1.getSpouse());
        assertNull(member2.getSpouse());

        verify(memberRepository, times(2)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when unlinking member without spouse")
    void unlinkSpouse_NoSpouse_ThrowsException() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.unlinkSpouse(1L, 1L)
        );

        assertEquals("Member is not linked to a spouse", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when unlinking member from different church")
    void unlinkSpouse_DifferentChurch_ThrowsException() {
        // Arrange
        Church otherChurch = new Church();
        otherChurch.setId(2L);
        member1.setChurch(otherChurch);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.unlinkSpouse(1L, 1L)
        );

        assertEquals("Member does not belong to your church", exception.getMessage());
    }

    @Test
    @DisplayName("Should return spouse when member has linked spouse")
    void getSpouse_ReturnsSpouse() {
        // Arrange
        member1.setSpouse(member2);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        // Act
        MemberResponse result = memberService.getSpouse(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Jane", result.firstName());
    }

    @Test
    @DisplayName("Should return null when member has no spouse")
    void getSpouse_NoSpouse_ReturnsNull() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        // Act
        MemberResponse result = memberService.getSpouse(1L, 1L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should throw exception when getting spouse of member from different church")
    void getSpouse_DifferentChurch_ThrowsException() {
        // Arrange
        Church otherChurch = new Church();
        otherChurch.setId(2L);
        member1.setChurch(otherChurch);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.getSpouse(1L, 1L)
        );

        assertEquals("Member does not belong to your church", exception.getMessage());
    }

    @Test
    @DisplayName("Should update marital status to married when linking spouse")
    void linkSpouse_UpdatesMaritalStatus() {
        // Arrange
        assertEquals("single", member1.getMaritalStatus());
        assertEquals("single", member2.getMaritalStatus());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileCompletenessService.calculateCompleteness(any(Member.class))).thenReturn(50);

        // Act
        memberService.linkSpouse(1L, 2L, 1L);

        // Assert
        assertEquals("married", member1.getMaritalStatus());
        assertEquals("married", member2.getMaritalStatus());
    }

    @Test
    @DisplayName("Should recalculate profile completeness after linking spouse")
    void linkSpouse_RecalculatesCompleteness() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileCompletenessService.calculateCompleteness(any(Member.class))).thenReturn(75);

        // Act
        memberService.linkSpouse(1L, 2L, 1L);

        // Assert
        verify(profileCompletenessService, times(2)).calculateCompleteness(any(Member.class));
        assertEquals(75, member1.getProfileCompleteness());
        assertEquals(75, member2.getProfileCompleteness());
    }
}
