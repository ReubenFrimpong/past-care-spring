package com.reuben.pastcare_spring.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest;
import com.reuben.pastcare_spring.dtos.AdvancedSearchResponse;
import com.reuben.pastcare_spring.dtos.HouseholdSuggestion;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.dtos.MemberStatsResponse;
import com.reuben.pastcare_spring.dtos.TagRequest;
import com.reuben.pastcare_spring.dtos.TagStatsResponse;
import com.reuben.pastcare_spring.dtos.ProfileCompletenessResponse;
import com.reuben.pastcare_spring.dtos.CompletenessStatsResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.services.HouseholdSuggestionService;
import com.reuben.pastcare_spring.services.ImageService;
import com.reuben.pastcare_spring.services.MemberService;
import com.reuben.pastcare_spring.util.RequestContextUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;




@RestController
@RequestMapping("/api/members")

public class MembersController {

  private final MemberService memberService;
  private final RequestContextUtil requestContextUtil;
  private final HouseholdSuggestionService householdSuggestionService;

  public MembersController(MemberService memberService, RequestContextUtil requestContextUtil, ImageService imageService, HouseholdSuggestionService householdSuggestionService){
    this.memberService = memberService;
    this.requestContextUtil = requestContextUtil;
    this.householdSuggestionService = householdSuggestionService;
  }

  @GetMapping
  @RequirePermission({Permission.MEMBER_VIEW_ALL, Permission.MEMBER_VIEW_FELLOWSHIP, Permission.MEMBER_VIEW_OWN})
  public ResponseEntity<Page<MemberResponse>> getMembers(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String filter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpServletRequest request) {

    Long churchId = requestContextUtil.extractChurchId(request);
    Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

    return ResponseEntity.ok(memberService.getMembers(churchId, search, filter, pageable));
  }

  @GetMapping("/stats")
  @RequirePermission({Permission.MEMBER_VIEW_ALL, Permission.MEMBER_VIEW_FELLOWSHIP})
  public ResponseEntity<MemberStatsResponse> getMemberStats(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    return ResponseEntity.ok(memberService.getMemberStats(churchId));
  }

  @GetMapping("/{id}")
  @RequirePermission({Permission.MEMBER_VIEW_ALL, Permission.MEMBER_VIEW_FELLOWSHIP, Permission.MEMBER_VIEW_OWN})
  public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
    return ResponseEntity.ok(memberService.getMemberById(id));
  }


  @PostMapping
  @RequirePermission(Permission.MEMBER_CREATE)
  public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest memberRequest) {
      return ResponseEntity.ok(memberService.createMember(memberRequest));
  }

  @PutMapping("/{id}")
  @RequirePermission({Permission.MEMBER_EDIT_ALL, Permission.MEMBER_EDIT_OWN, Permission.MEMBER_EDIT_PASTORAL})
  public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @RequestBody MemberRequest memberRequest) {
      return ResponseEntity.ok(memberService.updateMember(id, memberRequest));
  }

  @DeleteMapping("/{id}")
  @RequirePermission(Permission.MEMBER_DELETE)
  public ResponseEntity<?> deleteMember(@PathVariable Long id){
    memberService.deleteMember(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/profile-image")
  @RequirePermission({Permission.MEMBER_EDIT_ALL, Permission.MEMBER_EDIT_OWN})
  public ResponseEntity<?> uploadProfileImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
      return ResponseEntity.ok(memberService.uploadProfileImage(id, image));
  }

  /**
   * Quick add a member with minimal required information.
   * Used for fast member registration at events, church entrance, etc.
   *
   * @param request Quick add request with minimal fields (firstName, lastName, phone, sex)
   * @param httpRequest HTTP request to extract church ID
   * @return Created member response with status VISITOR and ~25% profile completeness
   */
  @PostMapping("/quick-add")
  @RequirePermission(Permission.MEMBER_CREATE)
  public ResponseEntity<MemberResponse> quickAddMember(
      @Valid @RequestBody com.reuben.pastcare_spring.dtos.MemberQuickAddRequest request,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.quickAddMember(request, churchId));
  }

  /**
   * Bulk import members from CSV/Excel file.
   * Supports column mapping, validation, duplicate detection, and partial imports.
   *
   * @param request Bulk import request with member data, column mapping, and options
   * @param httpRequest HTTP request to extract church ID
   * @return Import results with success/failure counts and error details
   */
  @PostMapping("/bulk-import")
  @RequirePermission(Permission.MEMBER_IMPORT)
  public ResponseEntity<com.reuben.pastcare_spring.dtos.MemberBulkImportResponse> bulkImportMembers(
      @Valid @RequestBody com.reuben.pastcare_spring.dtos.MemberBulkImportRequest request,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.bulkImportMembers(request, churchId));
  }

  /**
   * Bulk update multiple members with specified fields.
   * Supports updating fellowships, tags, status, and verification state.
   *
   * @param request Bulk update request with member IDs and fields to update
   * @return Update results with success/failure counts and error details
   */
  @PatchMapping("/bulk-update")
  @RequirePermission(Permission.MEMBER_EDIT_ALL)
  public ResponseEntity<com.reuben.pastcare_spring.dtos.MemberBulkUpdateResponse> bulkUpdateMembers(
      @Valid @RequestBody com.reuben.pastcare_spring.dtos.MemberBulkUpdateRequest request,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.bulkUpdateMembers(request, churchId));
  }

  /**
   * Bulk delete multiple members.
   * Permanently deletes selected members with church validation.
   *
   * @param requestBody Request body containing list of member IDs to delete
   * @param httpRequest HTTP request to extract church ID
   * @return Delete results with success/failure counts and error details
   */
  @DeleteMapping("/bulk-delete")
  public ResponseEntity<Map<String, Object>> bulkDeleteMembers(
      @RequestBody Map<String, List<Long>> requestBody,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    List<Long> memberIds = requestBody.get("memberIds");

    if (memberIds == null || memberIds.isEmpty()) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Member IDs are required");
      return ResponseEntity.badRequest().body(errorResponse);
    }

    return ResponseEntity.ok(memberService.bulkDeleteMembers(memberIds, churchId));
  }

  /**
   * Advanced search with dynamic filter criteria.
   * Supports complex queries with multiple filters, logical operators (AND/OR),
   * and nested filter groups.
   *
   * @param request Search request with filter criteria and logical operators
   * @param httpRequest HTTP request to extract church ID
   * @param page Page number (default: 0)
   * @param size Page size (default: 20)
   * @return Search response with filtered members and metadata
   */
  @PostMapping("/advanced-search")
  public ResponseEntity<AdvancedSearchResponse> advancedSearch(
      @Valid @RequestBody AdvancedSearchRequest request,
      HttpServletRequest httpRequest,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
    return ResponseEntity.ok(memberService.advancedSearch(request, churchId, pageable));
  }

  // ==================== Tag Management Endpoints ====================

  /**
   * Add tags to a member.
   *
   * @param id Member ID
   * @param request Tag request containing tags to add
   * @return Updated member response
   */
  @PostMapping("/{id}/tags")
  public ResponseEntity<MemberResponse> addTags(
      @PathVariable Long id,
      @Valid @RequestBody TagRequest request) {
    return ResponseEntity.ok(memberService.addTags(id, request.tags()));
  }

  /**
   * Remove tags from a member.
   *
   * @param id Member ID
   * @param request Tag request containing tags to remove
   * @return Updated member response
   */
  @DeleteMapping("/{id}/tags")
  public ResponseEntity<MemberResponse> removeTags(
      @PathVariable Long id,
      @Valid @RequestBody TagRequest request) {
    return ResponseEntity.ok(memberService.removeTags(id, request.tags()));
  }

  /**
   * Replace all tags for a member.
   *
   * @param id Member ID
   * @param request Tag request containing new tags
   * @return Updated member response
   */
  @PutMapping("/{id}/tags")
  public ResponseEntity<MemberResponse> setTags(
      @PathVariable Long id,
      @Valid @RequestBody TagRequest request) {
    return ResponseEntity.ok(memberService.setTags(id, request.tags()));
  }

  /**
   * Get all unique tags used in the church with usage statistics.
   *
   * @param httpRequest HTTP request to extract church ID
   * @return Tag statistics response with tag counts
   */
  @GetMapping("/tags")
  public ResponseEntity<TagStatsResponse> getAllTags(HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    Map<String, Long> tagCounts = memberService.getAllTags(churchId);

    long totalMembers = tagCounts.values().stream().mapToLong(Long::longValue).sum();

    return ResponseEntity.ok(new TagStatsResponse(
        tagCounts,
        tagCounts.size(),
        totalMembers
    ));
  }

  /**
   * Get all members with a specific tag.
   *
   * @param tag Tag to search for
   * @param httpRequest HTTP request to extract church ID
   * @param page Page number (default: 0)
   * @param size Page size (default: 20)
   * @return Page of members with the tag
   */
  @GetMapping("/tags/{tag}")
  public ResponseEntity<Page<MemberResponse>> getMembersByTag(
      @PathVariable String tag,
      HttpServletRequest httpRequest,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
    return ResponseEntity.ok(memberService.getMembersByTag(churchId, tag, pageable));
  }

  // ==================== Spouse Linking Endpoints ====================

  /**
   * Link two members as spouses (bidirectional).
   * Both members will be linked to each other and their marital status will be set to "married".
   *
   * @param id Member ID to link
   * @param spouseId Spouse member ID to link with
   * @param httpRequest HTTP request to extract church ID
   * @return Updated member response
   */
  @PostMapping("/{id}/spouse/{spouseId}")
  public ResponseEntity<MemberResponse> linkSpouse(
      @PathVariable Long id,
      @PathVariable Long spouseId,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.linkSpouse(id, spouseId, churchId));
  }

  /**
   * Unlink a member from their spouse (bidirectional).
   * Both members will have their spouse link removed.
   *
   * @param id Member ID to unlink
   * @param httpRequest HTTP request to extract church ID
   * @return Updated member response
   */
  @DeleteMapping("/{id}/spouse")
  public ResponseEntity<MemberResponse> unlinkSpouse(
      @PathVariable Long id,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.unlinkSpouse(id, churchId));
  }

  /**
   * Get the spouse of a member.
   *
   * @param id Member ID
   * @param httpRequest HTTP request to extract church ID
   * @return Spouse member response, or 204 No Content if not linked
   */
  @GetMapping("/{id}/spouse")
  public ResponseEntity<MemberResponse> getSpouse(
      @PathVariable Long id,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    MemberResponse spouse = memberService.getSpouse(id, churchId);
    if (spouse == null) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(spouse);
  }

  // ==================== Parent-Child Relationship Endpoints ====================

  /**
   * Add a parent to a child member.
   *
   * @param childId Child member ID
   * @param parentId Parent member ID
   * @param httpRequest HTTP request to extract church ID
   * @return Updated child member response with parents list
   */
  @PostMapping("/{childId}/parents/{parentId}")
  public ResponseEntity<MemberResponse> addParent(
      @PathVariable Long childId,
      @PathVariable Long parentId,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.addParent(childId, parentId, churchId));
  }

  /**
   * Remove a parent from a child member.
   *
   * @param childId Child member ID
   * @param parentId Parent member ID to remove
   * @param httpRequest HTTP request to extract church ID
   * @return Updated child member response
   */
  @DeleteMapping("/{childId}/parents/{parentId}")
  public ResponseEntity<MemberResponse> removeParent(
      @PathVariable Long childId,
      @PathVariable Long parentId,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.removeParent(childId, parentId, churchId));
  }

  /**
   * Get all parents of a child member.
   *
   * @param childId Child member ID
   * @param httpRequest HTTP request to extract church ID
   * @return List of parent member responses
   */
  @GetMapping("/{childId}/parents")
  public ResponseEntity<List<MemberResponse>> getParents(
      @PathVariable Long childId,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.getParents(childId, churchId));
  }

  /**
   * Get all children of a parent member.
   *
   * @param parentId Parent member ID
   * @param httpRequest HTTP request to extract church ID
   * @return List of child member responses
   */
  @GetMapping("/{parentId}/children")
  public ResponseEntity<List<MemberResponse>> getChildren(
      @PathVariable Long parentId,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.getChildren(parentId, churchId));
  }

  // ==================== Profile Completeness Endpoints ====================

  /**
   * Get profile completeness details for a specific member.
   *
   * @param id Member ID
   * @return Profile completeness with missing fields and suggestions
   */
  @GetMapping("/{id}/profile-completeness")
  public ResponseEntity<ProfileCompletenessResponse> getProfileCompleteness(@PathVariable Long id) {
    return ResponseEntity.ok(memberService.getProfileCompleteness(id));
  }

  /**
   * Get church-wide profile completeness statistics.
   *
   * @param httpRequest HTTP request to extract church ID
   * @return Completeness statistics including averages and distribution
   */
  @GetMapping("/stats/completeness")
  public ResponseEntity<CompletenessStatsResponse> getCompletenessStats(HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    return ResponseEntity.ok(memberService.getCompletenessStats(churchId));
  }

  /**
   * Get intelligent household suggestion based on member request data.
   * Analyzes spouse and children linkages to suggest appropriate household actions.
   *
   * @param memberRequest The member request containing family relation data
   * @param httpRequest HTTP request to extract church ID
   * @return HouseholdSuggestion with recommended action (JOIN, CREATE, or NONE)
   */
  @PostMapping("/household-suggestion")
  @RequirePermission(Permission.MEMBER_CREATE)
  public ResponseEntity<HouseholdSuggestion> getHouseholdSuggestion(
      @RequestBody MemberRequest memberRequest,
      HttpServletRequest httpRequest) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    HouseholdSuggestion suggestion = householdSuggestionService.suggestHouseholdForMember(memberRequest, churchId);
    return ResponseEntity.ok(suggestion);
  }
}
