package com.reuben.pastcare_spring.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.services.MemberService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@RequestMapping("/api/members")

public class MembersController {

  private MemberService memberService;

  public MembersController(MemberService memberService){
    this.memberService = memberService;
  }

  @GetMapping
  public ResponseEntity<List<MemberResponse>> getAllMembers() {
      return ResponseEntity.ok(memberService.getAllMembers());
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
}
