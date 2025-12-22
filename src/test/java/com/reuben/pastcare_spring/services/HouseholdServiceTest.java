package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Household;
import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.HouseholdRepository;
import com.reuben.pastcare_spring.repositories.LocationRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HouseholdService Tests")
class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChurchRepository churchRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private HouseholdService householdService;

    private Church church;
    private Member householdHead;
    private Member member2;
    private Household household;
    private Location location;

    @BeforeEach
    void setUp() {
        church = new Church();
        church.setId(1L);
        church.setName("Test Church");

        householdHead = new Member();
        householdHead.setId(1L);
        householdHead.setFirstName("John");
        householdHead.setLastName("Doe");
        householdHead.setPhoneNumber("+254700000001");
        householdHead.setSex("male");
        householdHead.setChurch(church);

        member2 = new Member();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Doe");
        member2.setPhoneNumber("+254700000002");
        member2.setSex("female");
        member2.setChurch(church);

        location = new Location();
        location.setId(1L);
        location.setCoordinates("0.0,0.0");
        location.setCity("Nairobi");
        location.setCountryCode("KE");
        location.setCountryName("Kenya");

        household = new Household();
        household.setId(1L);
        household.setChurch(church);
        household.setHouseholdName("The Doe Family");
        household.setHouseholdHead(householdHead);
        household.setSharedLocation(location);
        household.setEstablishedDate(LocalDate.of(2020, 1, 1));
        household.setMembers(new ArrayList<>(List.of(householdHead)));
    }

    @Test
    @DisplayName("Should create household successfully")
    void testCreateHousehold() {
        HouseholdRequest request = new HouseholdRequest(
            "The Doe Family",
            1L,
            List.of(1L, 2L),
            1L,
            "Family notes",
            LocalDate.of(2020, 1, 1),
            "doe@family.com",
            "+254700000000",
            null
        );

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.existsByChurchAndHouseholdNameIgnoreCase(church, "The Doe Family")).thenReturn(false);
        when(memberRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(householdHead));
        when(memberRepository.findByIdAndChurch(2L, church)).thenReturn(Optional.of(member2));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> {
            Household saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        HouseholdResponse response = householdService.createHousehold(1L, request);

        assertNotNull(response);
        assertEquals("The Doe Family", response.householdName());
        verify(householdRepository, times(2)).save(any(Household.class));
    }

    @Test
    @DisplayName("Should throw exception when household name already exists")
    void testCreateHouseholdDuplicateName() {
        HouseholdRequest request = new HouseholdRequest(
            "The Doe Family",
            1L,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.existsByChurchAndHouseholdNameIgnoreCase(church, "The Doe Family")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            householdService.createHousehold(1L, request)
        );
    }

    @Test
    @DisplayName("Should throw exception when church not found")
    void testCreateHouseholdChurchNotFound() {
        HouseholdRequest request = new HouseholdRequest(
            "The Doe Family",
            1L,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(churchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            householdService.createHousehold(999L, request)
        );
    }

    @Test
    @DisplayName("Should throw exception when household head not found")
    void testCreateHouseholdHeadNotFound() {
        HouseholdRequest request = new HouseholdRequest(
            "The Doe Family",
            999L,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.existsByChurchAndHouseholdNameIgnoreCase(church, "The Doe Family")).thenReturn(false);
        when(memberRepository.findByIdAndChurch(999L, church)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            householdService.createHousehold(1L, request)
        );
    }

    @Test
    @DisplayName("Should update household successfully")
    void testUpdateHousehold() {
        HouseholdRequest request = new HouseholdRequest(
            "The Doe Family Updated",
            1L,
            List.of(1L),
            null,
            "Updated notes",
            LocalDate.of(2021, 1, 1),
            "updated@family.com",
            "+254700000001",
            null
        );

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(household));
        when(householdRepository.existsByChurchAndHouseholdNameIgnoreCase(church, "The Doe Family Updated")).thenReturn(false);
        when(memberRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(householdHead));
        when(householdRepository.save(any(Household.class))).thenReturn(household);

        HouseholdResponse response = householdService.updateHousehold(1L, 1L, request);

        assertNotNull(response);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("Should get household by ID successfully")
    void testGetHouseholdById() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(household));

        HouseholdResponse response = householdService.getHouseholdById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("The Doe Family", response.householdName());
    }

    @Test
    @DisplayName("Should throw exception when household not found")
    void testGetHouseholdByIdNotFound() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(999L, church)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            householdService.getHouseholdById(1L, 999L)
        );
    }

    @Test
    @DisplayName("Should get all households with pagination")
    void testGetAllHouseholds() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Household> householdPage = new PageImpl<>(List.of(household), pageable, 1);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByChurch(church, pageable)).thenReturn(householdPage);

        Page<HouseholdSummaryResponse> response = householdService.getAllHouseholds(1L, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("The Doe Family", response.getContent().get(0).householdName());
    }

    @Test
    @DisplayName("Should search households by name")
    void testSearchHouseholds() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Household> householdPage = new PageImpl<>(List.of(household), pageable, 1);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByChurchAndHouseholdNameContainingIgnoreCase(church, "Doe", pageable))
            .thenReturn(householdPage);

        Page<HouseholdSummaryResponse> response = householdService.searchHouseholds(1L, "Doe", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should delete household successfully")
    void testDeleteHousehold() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(household));

        assertDoesNotThrow(() -> householdService.deleteHousehold(1L, 1L));

        verify(householdRepository).delete(household);
    }

    @Test
    @DisplayName("Should add member to household successfully")
    void testAddMemberToHousehold() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(household));
        when(memberRepository.findByIdAndChurch(2L, church)).thenReturn(Optional.of(member2));
        when(householdRepository.save(any(Household.class))).thenReturn(household);

        HouseholdResponse response = householdService.addMemberToHousehold(1L, 1L, 2L);

        assertNotNull(response);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("Should remove member from household successfully")
    void testRemoveMemberFromHousehold() {
        household.addMember(member2);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(household));
        when(memberRepository.findByIdAndChurch(2L, church)).thenReturn(Optional.of(member2));
        when(householdRepository.save(any(Household.class))).thenReturn(household);

        HouseholdResponse response = householdService.removeMemberFromHousehold(1L, 1L, 2L);

        assertNotNull(response);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    @DisplayName("Should throw exception when removing household head")
    void testRemoveHouseholdHead() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(household));
        when(memberRepository.findByIdAndChurch(1L, church)).thenReturn(Optional.of(householdHead));

        assertThrows(IllegalArgumentException.class, () ->
            householdService.removeMemberFromHousehold(1L, 1L, 1L)
        );
    }

    @Test
    @DisplayName("Should get household statistics")
    void testGetHouseholdStats() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.countByChurch(church)).thenReturn(10L);
        when(memberRepository.countByChurch(church)).thenReturn(50L);
        when(memberRepository.countByChurchAndHouseholdIsNotNull(church)).thenReturn(35L);

        HouseholdStatsResponse stats = householdService.getHouseholdStats(1L);

        assertNotNull(stats);
        assertEquals(10L, stats.totalHouseholds());
        assertEquals(50L, stats.totalMembers());
        assertEquals(35L, stats.membersInHouseholds());
        assertEquals(3.5, stats.averageHouseholdSize(), 0.01);
    }

    @Test
    @DisplayName("Should handle zero households in stats")
    void testGetHouseholdStatsZeroHouseholds() {
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(householdRepository.countByChurch(church)).thenReturn(0L);
        when(memberRepository.countByChurch(church)).thenReturn(50L);
        when(memberRepository.countByChurchAndHouseholdIsNotNull(church)).thenReturn(0L);

        HouseholdStatsResponse stats = householdService.getHouseholdStats(1L);

        assertNotNull(stats);
        assertEquals(0L, stats.totalHouseholds());
        assertEquals(0.0, stats.averageHouseholdSize(), 0.01);
    }
}
