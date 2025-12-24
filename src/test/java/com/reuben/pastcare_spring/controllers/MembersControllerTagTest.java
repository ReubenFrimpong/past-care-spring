package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.TagRequest;
import com.reuben.pastcare_spring.dtos.TagStatsResponse;
import com.reuben.pastcare_spring.models.MemberStatus;
import com.reuben.pastcare_spring.services.MemberService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for MembersController tag management endpoints.
 * Tests all tag-related REST API operations.
 */
@WebMvcTest(controllers = MembersController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Members Controller Tag Management Tests")
class MembersControllerTagTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private RequestContextUtil requestContextUtil;

    private MemberResponse memberResponse;
    private TagRequest tagRequest;

    @BeforeEach
    void setUp() {
        // Setup member response
        memberResponse = new MemberResponse(
            1L,
            "John",
            null,
            "Doe",
            null,
            "male",
            null,
            List.of(),
            null,
            "GH",
            "Africa/Accra",
            "+233241234567",
            null,
            null,
            null,
            null,
            "single",
            null,  // spouseId
            null,  // occupation
            null,
            null,
            null,
            null,
            true,
            MemberStatus.MEMBER,
            75,
            Set.of("youth", "choir"),
            null,  // parents
            null   // children
        );

        // Setup tag request
        tagRequest = new TagRequest(Set.of("youth", "choir"));

        // Mock request context
        when(requestContextUtil.extractChurchId(any(HttpServletRequest.class))).thenReturn(1L);
    }

    // ========== Add Tags ==========

    @Test
    @DisplayName("Should add tags to member successfully")
    void testAddTags() throws Exception {
        // Given
        when(memberService.addTags(eq(1L), anySet())).thenReturn(memberResponse);

        // When & Then
        mockMvc.perform(post("/api/members/1/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.tags[0]").value("youth"))
            .andExpect(jsonPath("$.tags[1]").value("choir"));

        verify(memberService).addTags(eq(1L), eq(tagRequest.tags()));
    }

    @Test
    @DisplayName("Should return 400 when tags are null")
    void testAddTags_NullTags() throws Exception {
        // Given
        TagRequest invalidRequest = new TagRequest(null);

        // When & Then
        mockMvc.perform(post("/api/members/1/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when tags are empty")
    void testAddTags_EmptyTags() throws Exception {
        // Given
        TagRequest invalidRequest = new TagRequest(new HashSet<>());

        // When & Then
        mockMvc.perform(post("/api/members/1/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ========== Remove Tags ==========

    @Test
    @DisplayName("Should remove tags from member successfully")
    void testRemoveTags() throws Exception {
        // Given
        MemberResponse updatedResponse = new MemberResponse(
            1L, "John", null, "Doe", null, "male", null, List.of(), null,
            "GH", "Africa/Accra", "+233241234567", null, null, null, null,
            "single", null, null, null, null, null, null, true,
            MemberStatus.MEMBER, 75, Set.of("choir"),
            null, null
        );
        when(memberService.removeTags(eq(1L), anySet())).thenReturn(updatedResponse);

        TagRequest removeRequest = new TagRequest(Set.of("youth"));

        // When & Then
        mockMvc.perform(post("/api/members/1/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(removeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.tags[0]").value("choir"));

        verify(memberService).removeTags(eq(1L), eq(removeRequest.tags()));
    }

    // ========== Set Tags ==========

    @Test
    @DisplayName("Should set tags for member successfully")
    void testSetTags() throws Exception {
        // Given
        MemberResponse updatedResponse = new MemberResponse(
            1L, "John", null, "Doe", null, "male", null, List.of(), null,
            "GH", "Africa/Accra", "+233241234567", null, null, null, null,
            "single", null, null, null, null, null, null, true,
            MemberStatus.MEMBER, 75, Set.of("leader", "worship"),
            null, null
        );
        when(memberService.setTags(eq(1L), anySet())).thenReturn(updatedResponse);

        TagRequest setRequest = new TagRequest(Set.of("leader", "worship"));

        // When & Then
        mockMvc.perform(post("/api/members/1/tags/set")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(setRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.tags.length()").value(2));

        verify(memberService).setTags(eq(1L), eq(setRequest.tags()));
    }

    // ========== Get All Tags ==========

    @Test
    @DisplayName("Should get all tags with statistics")
    void testGetAllTags() throws Exception {
        // Given
        Map<String, Long> tagCounts = new LinkedHashMap<>();
        tagCounts.put("youth", 15L);
        tagCounts.put("choir", 10L);
        tagCounts.put("leader", 5L);

        when(memberService.getAllTags(1L)).thenReturn(tagCounts);

        // When & Then
        mockMvc.perform(get("/api/members/tags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTags").value(3))
            .andExpect(jsonPath("$.totalMembers").value(30))
            .andExpect(jsonPath("$.tags.youth").value(15))
            .andExpect(jsonPath("$.tags.choir").value(10))
            .andExpect(jsonPath("$.tags.leader").value(5));

        verify(memberService).getAllTags(1L);
    }

    @Test
    @DisplayName("Should return empty statistics when no tags exist")
    void testGetAllTags_Empty() throws Exception {
        // Given
        when(memberService.getAllTags(1L)).thenReturn(new HashMap<>());

        // When & Then
        mockMvc.perform(get("/api/members/tags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTags").value(0))
            .andExpect(jsonPath("$.totalMembers").value(0))
            .andExpect(jsonPath("$.tags").isEmpty());

        verify(memberService).getAllTags(1L);
    }

    // ========== Get Members By Tag ==========

    @Test
    @DisplayName("Should get members by tag")
    void testGetMembersByTag() throws Exception {
        // Given
        List<MemberResponse> members = List.of(memberResponse);
        Page<MemberResponse> membersPage = new PageImpl<>(members);

        when(memberService.getMembersByTag(eq(1L), eq("youth"), any())).thenReturn(membersPage);

        // When & Then
        mockMvc.perform(get("/api/members/tags/youth")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].firstName").value("John"))
            .andExpect(jsonPath("$.content[0].tags[0]").value("youth"))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(memberService).getMembersByTag(eq(1L), eq("youth"), any());
    }

    @Test
    @DisplayName("Should return empty page when no members have tag")
    void testGetMembersByTag_Empty() throws Exception {
        // Given
        when(memberService.getMembersByTag(eq(1L), eq("nonexistent"), any()))
            .thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/members/tags/nonexistent")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));

        verify(memberService).getMembersByTag(eq(1L), eq("nonexistent"), any());
    }

    @Test
    @DisplayName("Should use default pagination parameters")
    void testGetMembersByTag_DefaultPagination() throws Exception {
        // Given
        when(memberService.getMembersByTag(eq(1L), eq("youth"), any()))
            .thenReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/members/tags/youth"))
            .andExpect(status().isOk());

        verify(memberService).getMembersByTag(eq(1L), eq("youth"), any());
    }
}
