package com.reuben.pastcare_spring.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.dtos.MemberStatsResponse;
import com.reuben.pastcare_spring.security.JwtUtil;
import com.reuben.pastcare_spring.services.ImageService;
import com.reuben.pastcare_spring.services.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
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
  private final JwtUtil jwtUtil;
  private final ImageService imageService;

  public MembersController(MemberService memberService, JwtUtil jwtUtil, ImageService imageService){
    this.memberService = memberService;
    this.jwtUtil = jwtUtil;
    this.imageService = imageService;
  }

  @GetMapping
  public ResponseEntity<Page<MemberResponse>> getMembers(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String filter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      HttpServletRequest request) {

    Long churchId = extractChurchIdFromRequest(request);
    Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

    return ResponseEntity.ok(memberService.getMembers(churchId, search, filter, pageable));
  }

  @GetMapping("/stats")
  public ResponseEntity<MemberStatsResponse> getMemberStats(HttpServletRequest request) {
    Long churchId = extractChurchIdFromRequest(request);
    return ResponseEntity.ok(memberService.getMemberStats(churchId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
    return ResponseEntity.ok(memberService.getMemberById(id));
  }
  

  @PostMapping
  public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest memberRequest) {
      return ResponseEntity.ok(memberService.createMember(memberRequest));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @RequestBody MemberRequest memberRequest) {
      return ResponseEntity.ok(memberService.updateMember(id, memberRequest));
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteMember(@PathVariable Long id){
    memberService.deleteMember(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/profile-image")
  public ResponseEntity<?> uploadProfileImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
      return ResponseEntity.ok(memberService.uploadProfileImage(id, image)); 
  }

  /**
   * Extract church ID from JWT token in request.
   * Checks cookies for access_token.
   */
  private Long extractChurchIdFromRequest(HttpServletRequest request) {
    String token = null;

    // Try cookie
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token != null) {
      return jwtUtil.extractChurchId(token);
    }

    throw new RuntimeException("No valid JWT token found");
  }
}
