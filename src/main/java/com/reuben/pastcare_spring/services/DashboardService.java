package com.reuben.pastcare_spring.services;

import com.github.javafaker.Faker;
import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dashboard service providing aggregated data for church management dashboard.
 * Uses JavaFaker to generate realistic test data for demonstration.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final UserRepository userRepository;
  private final Faker faker = new Faker();
  private final Random random = new Random();

  /**
   * Get complete dashboard data for the current user.
   *
   * @param userId Current user ID from JWT
   * @return Complete dashboard data with stats, care needs, events, and activities
   */
  public DashboardResponse getDashboardData(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return new DashboardResponse(
        user.getName(),
        generateStats(),
        generatePastoralCareNeeds(),
        generateUpcomingEvents(),
        generateRecentActivities()
    );
  }

  /**
   * Generate dashboard statistics using JavaFaker.
   */
  private DashboardStatsResponse generateStats() {
    int activeMembers = faker.number().numberBetween(200, 500);
    int needPrayer = faker.number().numberBetween(5, 20);
    int eventsThisWeek = faker.number().numberBetween(3, 10);
    int attendanceRate = faker.number().numberBetween(75, 95);

    return new DashboardStatsResponse(
        activeMembers,
        needPrayer,
        eventsThisWeek,
        attendanceRate + "%"
    );
  }

  /**
   * Generate pastoral care needs using JavaFaker.
   */
  private List<PastoralCareNeedResponse> generatePastoralCareNeeds() {
    List<PastoralCareNeedResponse> needs = new ArrayList<>();
    String[] priorities = {"Urgent", "Today", "This Week"};
    String[] careTypes = {
        "Surgery recovery - needs visit",
        "Job loss - needs counseling",
        "Family crisis - prayer request",
        "Health concerns - needs support",
        "Bereavement - needs comfort",
        "Marriage counseling needed",
        "Financial difficulty - needs help",
        "Mental health - needs prayer"
    };

    for (int i = 0; i < 3; i++) {
      needs.add(new PastoralCareNeedResponse(
          (long) i + 1,
          faker.name().fullName(),
          careTypes[random.nextInt(careTypes.length)],
          priorities[random.nextInt(priorities.length)]
      ));
    }

    return needs;
  }

  /**
   * Generate upcoming events using JavaFaker.
   */
  private List<UpcomingEventResponse> generateUpcomingEvents() {
    List<UpcomingEventResponse> events = new ArrayList<>();
    String[] eventTypes = {
        "Sunday Service",
        "Youth Bible Study",
        "Community Outreach",
        "Prayer Meeting",
        "Women's Fellowship",
        "Men's Breakfast",
        "Choir Practice",
        "Leadership Meeting"
    };

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("EEEE 'at' h:mm a");

    for (int i = 0; i < 3; i++) {
      LocalDateTime eventTime = now.plusDays(i + 1).withHour(faker.number().numberBetween(9, 19))
          .withMinute(0).withSecond(0);
      String badge = i == 0 ? "Tomorrow" : "";

      events.add(new UpcomingEventResponse(
          (long) i + 1,
          eventTypes[random.nextInt(eventTypes.length)],
          eventTime.format(timeFormatter),
          eventTime,
          badge
      ));
    }

    return events;
  }

  /**
   * Generate recent activities using JavaFaker.
   */
  private List<RecentActivityResponse> generateRecentActivities() {
    List<RecentActivityResponse> activities = new ArrayList<>();

    // New member activity
    activities.add(new RecentActivityResponse(
        1L,
        "New Member",
        "New Member",
        faker.name().fullName() + " joined"
    ));

    // Donation activity
    int donationAmount = faker.number().numberBetween(100, 1000);
    activities.add(new RecentActivityResponse(
        2L,
        "Donation Received",
        "Donation Received",
        "$" + donationAmount + " from " + (random.nextBoolean() ? faker.name().fullName() : "Anonymous")
    ));

    // Attendance activity
    int attendanceCount = faker.number().numberBetween(150, 300);
    activities.add(new RecentActivityResponse(
        3L,
        "Attendance Updated",
        "Attendance Updated",
        "Sunday service - " + attendanceCount + " present"
    ));

    return activities;
  }

  /**
   * Get dashboard statistics only.
   */
  public DashboardStatsResponse getStats() {
    return generateStats();
  }

  /**
   * Get pastoral care needs only.
   */
  public List<PastoralCareNeedResponse> getPastoralCareNeeds() {
    return generatePastoralCareNeeds();
  }

  /**
   * Get upcoming events only.
   */
  public List<UpcomingEventResponse> getUpcomingEvents() {
    return generateUpcomingEvents();
  }

  /**
   * Get recent activities only.
   */
  public List<RecentActivityResponse> getRecentActivities() {
    return generateRecentActivities();
  }
}
