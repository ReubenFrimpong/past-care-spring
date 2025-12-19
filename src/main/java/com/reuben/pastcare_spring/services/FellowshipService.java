package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FellowshipService {

  private final FellowshipRepository fellowshipRepository;

  public List<Fellowship> getAllFellowships() {
    return fellowshipRepository.findAll();
  }

  public Fellowship getFellowshipById(Long id) {
    return fellowshipRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + id));
  }

  public Fellowship createFellowship(Fellowship fellowship) {
    return fellowshipRepository.save(fellowship);
  }

  public Fellowship updateFellowship(Long id, Fellowship fellowship) {
    Fellowship existingFellowship = getFellowshipById(id);
    existingFellowship.setName(fellowship.getName());
    return fellowshipRepository.save(existingFellowship);
  }

  public void deleteFellowship(Long id) {
    Fellowship fellowship = getFellowshipById(id);
    fellowshipRepository.delete(fellowship);
  }
}
