package com.reuben.pastcare_spring.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.MemberDto;
import com.reuben.pastcare_spring.requests.MemberRequest;
import com.reuben.pastcare_spring.services.MemberService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
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
  public ResponseEntity<List<MemberDto>> getAllMembers() {
      var members = memberService.getAllMembers();
      return ResponseEntity.status(HttpStatus.OK).body(members);
  }
  

  @PostMapping
  public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberRequest memberRequest) {
      MemberDto createdeMember = memberService.createMember(memberRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdeMember);
  }

  @PutMapping("/{id}")
  public ResponseEntity<MemberDto> updateMember(@PathVariable Integer id, @RequestBody MemberRequest memberRequest) {
      var updatedMember = memberService.updateMember(id, memberRequest);
      return ResponseEntity.status(HttpStatus.OK).body(updatedMember);
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteMember(@PathVariable Integer id){
    memberService.deleteMember(id);
    return ResponseEntity.noContent().build();
  }
}
