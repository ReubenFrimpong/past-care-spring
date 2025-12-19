package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.services.FellowshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fellowships")
@RequiredArgsConstructor
@Tag(name = "Fellowship", description = "Fellowship management endpoints")
public class FellowshipController {

  private final FellowshipService fellowshipService;

  /**
   * Get all fellowships for the current user's church.
   *
   * @return List of fellowships
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get all fellowships", description = "Returns all fellowships for the current user's church")
  public ResponseEntity<List<Fellowship>> getAllFellowships() {
    List<Fellowship> fellowships = fellowshipService.getAllFellowships();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowship by ID.
   *
   * @param id Fellowship ID
   * @return Fellowship
   */
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowship by ID", description = "Returns a single fellowship by ID")
  public ResponseEntity<Fellowship> getFellowshipById(@PathVariable Long id) {
    Fellowship fellowship = fellowshipService.getFellowshipById(id);
    return ResponseEntity.ok(fellowship);
  }

  /**
   * Create a new fellowship.
   *
   * @param fellowship Fellowship to create
   * @return Created fellowship
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create fellowship", description = "Creates a new fellowship")
  public ResponseEntity<Fellowship> createFellowship(@RequestBody Fellowship fellowship) {
    Fellowship createdFellowship = fellowshipService.createFellowship(fellowship);
    return ResponseEntity.ok(createdFellowship);
  }

  /**
   * Update an existing fellowship.
   *
   * @param id Fellowship ID
   * @param fellowship Fellowship data
   * @return Updated fellowship
   */
  @PutMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update fellowship", description = "Updates an existing fellowship")
  public ResponseEntity<Fellowship> updateFellowship(@PathVariable Long id, @RequestBody Fellowship fellowship) {
    Fellowship updatedFellowship = fellowshipService.updateFellowship(id, fellowship);
    return ResponseEntity.ok(updatedFellowship);
  }

  /**
   * Delete a fellowship.
   *
   * @param id Fellowship ID
   * @return No content
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Delete fellowship", description = "Deletes a fellowship")
  public ResponseEntity<Void> deleteFellowship(@PathVariable Long id) {
    fellowshipService.deleteFellowship(id);
    return ResponseEntity.noContent().build();
  }
}
