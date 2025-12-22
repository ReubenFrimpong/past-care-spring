package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.TagRequest;
import com.reuben.pastcare_spring.models.MemberStatus;
import com.reuben.pastcare_spring.services.MemberService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembersController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tag Controller Tests")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private RequestContextUtil requestContextUtil;

    private MemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        memberResponse = new MemberResponse(
                1L, "John", null, "Doe", "Mr", "Male", null, null,
                null, "GH", "Africa/Accra", "+233241111111", null, null,
                null, null, "single", null, null, null,  // added spouseId (null)
                null, null, null, null, true,
                MemberStatus.MEMBER, 75, new HashSet<>(Arrays.asList("youth", "choir"))
        );

        when(requestContextUtil.extractChurchId(any())).thenReturn(1L);
    }

    @Test
    @DisplayName("Should add tags to a member")
    void testAddTags() throws Exception {
        Set<String> tags = new HashSet<>(Arrays.asList("leader", "teacher"));
        TagRequest request = new TagRequest(tags);

        when(memberService.addTags(eq(1L), any())).thenReturn(memberResponse);

        mockMvc.perform(post("/api/members/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("Should remove tags from a member")
    void testRemoveTags() throws Exception {
        Set<String> tags = new HashSet<>(Arrays.asList("choir"));
        TagRequest request = new TagRequest(tags);

        when(memberService.removeTags(eq(1L), any())).thenReturn(memberResponse);

        mockMvc.perform(delete("/api/members/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Should set tags for a member")
    void testSetTags() throws Exception {
        Set<String> tags = new HashSet<>(Arrays.asList("new-tag1", "new-tag2"));
        TagRequest request = new TagRequest(tags);

        when(memberService.setTags(eq(1L), any())).thenReturn(memberResponse);

        mockMvc.perform(put("/api/members/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Should get all tags with statistics")
    void testGetAllTags() throws Exception {
        Map<String, Long> tagCounts = new HashMap<>();
        tagCounts.put("youth", 10L);
        tagCounts.put("choir", 15L);
        tagCounts.put("usher", 8L);

        when(memberService.getAllTags(1L)).thenReturn(tagCounts);

        mockMvc.perform(get("/api/members/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTags").value(3))
                .andExpect(jsonPath("$.tags.youth").value(10))
                .andExpect(jsonPath("$.tags.choir").value(15))
                .andExpect(jsonPath("$.tags.usher").value(8));
    }

    @Test
    @DisplayName("Should get members by tag")
    void testGetMembersByTag() throws Exception {
        List<MemberResponse> members = Arrays.asList(memberResponse);
        Page<MemberResponse> page = new PageImpl<>(members, PageRequest.of(0, 20), 1);

        when(memberService.getMembersByTag(eq(1L), eq("youth"), any())).thenReturn(page);

        mockMvc.perform(get("/api/members/tags/youth")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should reject empty tag set when adding tags")
    void testAddTagsValidation() throws Exception {
        Set<String> emptyTags = new HashSet<>();
        TagRequest request = new TagRequest(emptyTags);

        mockMvc.perform(post("/api/members/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject null tags when adding tags")
    void testAddTagsNullValidation() throws Exception {
        mockMvc.perform(post("/api/members/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tags\": null}"))
                .andExpect(status().isBadRequest());
    }
}
