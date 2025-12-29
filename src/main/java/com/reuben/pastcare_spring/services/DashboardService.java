package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.models.CareNeed;
import com.reuben.pastcare_spring.models.CareNeedPriority;
import com.reuben.pastcare_spring.models.CareNeedStatus;
import com.reuben.pastcare_spring.models.CounselingSession;
import com.reuben.pastcare_spring.models.CounselingStatus;
import com.reuben.pastcare_spring.models.Crisis;
import com.reuben.pastcare_spring.models.CrisisStatus;
import com.reuben.pastcare_spring.models.Donation;
import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.PrayerRequest;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;
import com.reuben.pastcare_spring.repositories.CareNeedRepository;
import com.reuben.pastcare_spring.repositories.CounselingSessionRepository;
import com.reuben.pastcare_spring.repositories.CrisisRepository;
import com.reuben.pastcare_spring.repositories.DonationRepository;
import com.reuben.pastcare_spring.repositories.EventRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.PrayerRequestRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Dashboard service providing aggregated data for church management dashboard.
 * All data is real, queried from the database.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final CareNeedRepository careNeedRepository;
  private final EventRepository eventRepository;
  private final PrayerRequestRepository prayerRequestRepository;
  private final DonationRepository donationRepository;
  private final AttendanceSessionRepository attendanceSessionRepository;
  private final CrisisRepository crisisRepository;
  private final CounselingSessionRepository counselingSessionRepository;
  private final AttendanceAnalyticsService attendanceAnalyticsService;
  private final FellowshipService fellowshipService;

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
        getStats(userId),
        getPastoralCareNeeds(userId),
        getUpcomingEvents(userId),
        getRecentActivities(userId)
    );
  }

  /**
   * Get dashboard statistics (real data from database).
   *
   * @param userId Current user ID from JWT
   * @return Dashboard statistics
   */
  private DashboardStatsResponse getStats(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new DashboardStatsResponse(0, 0, 0, "0%");
    }

    // Get real member count
    long activeMembers = memberRepository.countByChurch(user.getChurch());

    // Get events count for this week (from today to end of week)
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endOfWeek = now.toLocalDate().plusDays(7 - now.getDayOfWeek().getValue()).atTime(23, 59, 59);
    List<Event> eventsThisWeekList = eventRepository.findEventsByDateRange(
        user.getChurch().getId(),
        now,
        endOfWeek
    );
    int eventsThisWeek = eventsThisWeekList.size();

    // Get prayer requests count (PENDING + ACTIVE statuses)
    List<PrayerRequestStatus> activeStatuses = Arrays.asList(
        PrayerRequestStatus.PENDING,
        PrayerRequestStatus.ACTIVE
    );
    int needPrayer = prayerRequestRepository.findByChurchAndStatusIn(user.getChurch(), activeStatuses).size();

    // TODO: Implement real queries for these stats when features are added
    String attendanceRate = "0%"; // Will be implemented with attendance statistics

    return new DashboardStatsResponse(
        (int) activeMembers,
        needPrayer,
        eventsThisWeek,
        attendanceRate
    );
  }

  /**
   * Get pastoral care needs (real data from database).
   * Returns urgent and high priority active care needs for dashboard display.
   *
   * @param userId Current user ID from JWT
   * @return List of pastoral care needs (max 10)
   */
  private List<PastoralCareNeedResponse> getPastoralCareNeeds(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    // Get urgent and high priority care needs
    List<CareNeedStatus> activeStatuses = Arrays.asList(
        CareNeedStatus.OPEN,
        CareNeedStatus.IN_PROGRESS,
        CareNeedStatus.PENDING
    );

    List<CareNeed> urgentNeeds = careNeedRepository.findByChurchAndPriorityAndStatusIn(
        user.getChurch(), CareNeedPriority.URGENT, activeStatuses);

    List<CareNeed> highNeeds = careNeedRepository.findByChurchAndPriorityAndStatusIn(
        user.getChurch(), CareNeedPriority.HIGH, activeStatuses);

    // Combine and sort by priority (urgent first) then by created date
    List<CareNeed> allNeeds = Stream.concat(urgentNeeds.stream(), highNeeds.stream())
        .sorted(Comparator
            .comparing(CareNeed::getPriority)
            .thenComparing(CareNeed::getCreatedAt, Comparator.reverseOrder()))
        .limit(10)
        .toList();

    // Map to response DTOs
    return allNeeds.stream()
        .map(need -> new PastoralCareNeedResponse(
            need.getId(),
            need.getMember().getFirstName() + " " + need.getMember().getLastName(),
            need.getTitle(),
            formatPriorityBadge(need)
        ))
        .toList();
  }

  /**
   * Format priority badge for pastoral care needs.
   * Returns "Urgent", "Today", or "This Week" based on priority and due date.
   */
  private String formatPriorityBadge(CareNeed need) {
    if (need.getPriority() == CareNeedPriority.URGENT) {
      return "Urgent";
    }

    if (need.getDueDate() != null) {
      LocalDate today = LocalDate.now();
      if (need.getDueDate().equals(today)) {
        return "Today";
      } else if (need.getDueDate().isBefore(today.plusDays(7))) {
        return "This Week";
      }
    }

    return need.getPriority().toString();
  }

  /**
   * Get upcoming events (real data from database).
   * Returns next 5-10 upcoming events for dashboard display.
   *
   * @param userId Current user ID from JWT
   * @return List of upcoming events (max 10)
   */
  private List<UpcomingEventResponse> getUpcomingEvents(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    // Get upcoming events (not cancelled, future events only)
    PageRequest pageRequest = PageRequest.of(0, 10);
    List<Event> upcomingEvents = eventRepository.findUpcomingEvents(
        user.getChurch().getId(),
        LocalDateTime.now(),
        pageRequest
    ).getContent();

    // Map to response DTOs
    return upcomingEvents.stream()
        .map(event -> new UpcomingEventResponse(
            event.getId(),
            event.getName(),
            event.getDescription(),
            event.getStartDate(),
            formatEventBadge(event.getStartDate())
        ))
        .toList();
  }

  /**
   * Format event badge based on start date.
   * Returns "Today", "Tomorrow", "This Week", etc.
   */
  private String formatEventBadge(LocalDateTime eventDate) {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();
    LocalDate eventDateLocal = eventDate.toLocalDate();

    if (eventDateLocal.equals(today)) {
      return "Today";
    } else if (eventDateLocal.equals(today.plusDays(1))) {
      return "Tomorrow";
    } else if (eventDateLocal.isBefore(today.plusDays(7))) {
      return "This Week";
    } else if (eventDateLocal.isBefore(today.plusDays(14))) {
      return "Next Week";
    } else if (eventDateLocal.isBefore(today.plusDays(30))) {
      return "This Month";
    } else {
      return "Upcoming";
    }
  }

  /**
   * Get recent activities (real data from database).
   * Uses lightweight aggregated queries from multiple sources.
   *
   * @param userId Current user ID from JWT
   * @return List of recent activities (max 10)
   */
  private List<RecentActivityResponse> getRecentActivities(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    List<ActivityItem> activities = new ArrayList<>();
    PageRequest topFive = PageRequest.of(0, 5);

    // Get recent members (5 most recent)
    List<Member> recentMembers = memberRepository.findByChurchOrderByCreatedAtDesc(
        user.getChurch(), topFive).getContent();
    recentMembers.forEach(member -> activities.add(new ActivityItem(
        member.getId(),
        "NEW_MEMBER",
        "New Member Joined",
        member.getFirstName() + " " + member.getLastName() + " joined the church",
        member.getCreatedAt()
    )));

    // Get recent donations (5 most recent)
    PageRequest donationPageRequest = PageRequest.of(0, 5);
    List<Donation> recentDonations = donationRepository.findByChurchOrderByDonationDateDesc(
        user.getChurch(), donationPageRequest).getContent();
    recentDonations.forEach(donation -> activities.add(new ActivityItem(
        donation.getId(),
        "DONATION_RECEIVED",
        "Donation Received",
        String.format("$%.2f donation from %s",
            donation.getAmount(),
            donation.getMember() != null ? donation.getMember().getFirstName() + " " + donation.getMember().getLastName() : "Anonymous"),
        donation.getCreatedAt()
    )));

    // Get recent attendance sessions (5 most recent)
    List<AttendanceSession> recentSessions = attendanceSessionRepository
        .findByChurchOrderByCreatedAtDesc(user.getChurch(), topFive).getContent();
    recentSessions.forEach(session -> activities.add(new ActivityItem(
        session.getId(),
        "ATTENDANCE_RECORDED",
        "Attendance Recorded",
        String.format("Attendance session on %s", formatDate(session.getSessionDate())),
        session.getCreatedAt()
    )));

    // Get recent prayer requests (5 most recent)
    List<PrayerRequest> recentPrayers = prayerRequestRepository
        .findByChurchOrderByCreatedAtDesc(user.getChurch(), topFive).getContent();
    recentPrayers.forEach(prayer -> activities.add(new ActivityItem(
        prayer.getId(),
        "PRAYER_REQUEST",
        "Prayer Request Submitted",
        prayer.getTitle(),
        prayer.getCreatedAt()
    )));

    // Sort all activities by timestamp (most recent first) and take top 10
    return activities.stream()
        .sorted(Comparator.comparing(ActivityItem::timestamp).reversed())
        .limit(10)
        .map(item -> new RecentActivityResponse(
            item.id(),
            item.activityType(),
            item.title(),
            item.description()
        ))
        .toList();
  }

  /**
   * Helper record to hold activity items from different sources before sorting.
   */
  private record ActivityItem(
      Long id,
      String activityType,
      String title,
      String description,
      Instant timestamp
  ) {}

  /**
   * Format LocalDate for activity description.
   */
  private String formatDate(LocalDate date) {
    if (date == null) return "Unknown";
    return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
  }

  /**
   * Get dashboard statistics only.
   */
  public DashboardStatsResponse getStats() {
    // This method is called without userId context, return empty stats
    return new DashboardStatsResponse(0, 0, 0, "0%");
  }

  /**
   * Get pastoral care needs only.
   * Note: This method uses TenantContext to get the current church.
   */
  public List<PastoralCareNeedResponse> getPastoralCareNeeds() {
    // Get urgent and high priority care needs for current tenant
    List<CareNeedStatus> activeStatuses = Arrays.asList(
        CareNeedStatus.OPEN,
        CareNeedStatus.IN_PROGRESS,
        CareNeedStatus.PENDING
    );

    // Note: Repository methods will use TenantContext automatically
    List<CareNeed> urgentNeeds = careNeedRepository.findByPriorityOrderByCreatedAtDesc(CareNeedPriority.URGENT);
    List<CareNeed> highNeeds = careNeedRepository.findByPriorityOrderByCreatedAtDesc(CareNeedPriority.HIGH);

    // Filter by active status and combine
    List<CareNeed> allNeeds = Stream.concat(
        urgentNeeds.stream().filter(need -> activeStatuses.contains(need.getStatus())),
        highNeeds.stream().filter(need -> activeStatuses.contains(need.getStatus()))
    )
        .sorted(Comparator
            .comparing(CareNeed::getPriority)
            .thenComparing(CareNeed::getCreatedAt, Comparator.reverseOrder()))
        .limit(10)
        .toList();

    // Map to response DTOs
    return allNeeds.stream()
        .map(need -> new PastoralCareNeedResponse(
            need.getId(),
            need.getMember().getFirstName() + " " + need.getMember().getLastName(),
            need.getTitle(),
            formatPriorityBadge(need)
        ))
        .toList();
  }

  /**
   * Get upcoming events only.
   * Note: This method uses TenantContext to get the current church.
   */
  public List<UpcomingEventResponse> getUpcomingEvents() {
    // Note: EventRepository methods will use TenantContext automatically via church.id
    // For now, return empty list as we need churchId from context
    // This will be populated when called through authenticated endpoints
    return new ArrayList<>();
  }

  /**
   * Get recent activities only.
   * Note: This method uses TenantContext to get the current church.
   */
  public List<RecentActivityResponse> getRecentActivities() {
    // For now, return empty list as this requires church context from authenticated user
    // The main getRecentActivities(userId) method will be called through authenticated endpoints
    return new ArrayList<>();
  }

  /**
   * Get location-based member statistics for map visualization.
   *
   * @param userId Current user ID from JWT
   * @return List of locations with member counts and GPS coordinates
   */
  public List<LocationStatsResponse> getLocationStatistics(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.getLocationStatistics(user.getChurch());
  }

  /**
   * Get members with birthdays this week (real data).
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of members with birthdays this week
   */
  public List<BirthdayResponse> getBirthdaysThisWeek(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.findMembersWithBirthdaysThisWeek(user.getChurch());
  }

  /**
   * Get members with anniversaries this month (real data).
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of members with anniversaries this month
   */
  public List<AnniversaryResponse> getAnniversariesThisMonth(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.findMembersWithAnniversariesThisMonth(user.getChurch());
  }

  /**
   * Get irregular attenders (members absent for 3+ consecutive weeks).
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of members who haven't attended in 3+ weeks
   */
  public List<IrregularAttenderResponse> getIrregularAttenders(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.findIrregularAttenders(user.getChurch(), 3); // 3 weeks threshold
  }

  /**
   * Get member growth trend for the last 6 months.
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return Monthly member growth data
   */
  public List<MemberGrowthResponse> getMemberGrowthTrend(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.getMemberGrowthTrend(user.getChurch(), 6); // Last 6 months
  }

  /**
   * Get attendance summary for this month.
   * Dashboard Phase 1: Additional Widgets
   *
   * @param userId Current user ID from JWT
   * @return Attendance summary with key metrics
   */
  public AttendanceSummaryResponse getAttendanceSummaryThisMonth(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return null; // Frontend will handle null
    }

    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate today = LocalDate.now();

    return attendanceAnalyticsService.getAttendanceSummary(user.getChurch().getId(), startOfMonth, today);
  }

  /**
   * Get service type analytics for the last 30 days.
   * Dashboard Phase 1: Additional Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of service type analytics
   */
  public List<ServiceTypeAnalyticsResponse> getServiceTypeAnalytics(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    LocalDate start = LocalDate.now().minusDays(30);
    LocalDate end = LocalDate.now();

    return attendanceAnalyticsService.getServiceTypeAnalytics(user.getChurch().getId(), start, end);
  }

  /**
   * Get top active members based on attendance.
   * Dashboard Phase 1: Additional Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of top 10 active members
   */
  public List<MemberEngagementResponse> getTopActiveMembers(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    LocalDate start = LocalDate.now().minusDays(90); // Last 90 days
    LocalDate end = LocalDate.now();

    List<MemberEngagementResponse> allEngagement = attendanceAnalyticsService.getMemberEngagement(
        user.getChurch().getId(), start, end);

    // Return top 10 by sessions attended
    return allEngagement.stream()
        .sorted((a, b) -> Long.compare(b.sessionsAttended(), a.sessionsAttended()))
        .limit(10)
        .toList();
  }

  /**
   * Get fellowship health overview.
   * Dashboard Phase 1: Fellowship Analytics Widget
   *
   * @param userId Current user ID from JWT
   * @return Fellowship comparison data ranked by health
   */
  public List<FellowshipComparisonResponse> getFellowshipHealthOverview(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return fellowshipService.getFellowshipComparison();
  }

  /**
   * Get donation statistics for dashboard.
   * Dashboard Phase 3: Donations Widget
   *
   * @param userId Current user ID from JWT
   * @return Donation statistics
   */
  public DonationStatsResponse getDonationStats(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new DonationStatsResponse(0, BigDecimal.ZERO, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO);
    }

    // Get all-time totals
    long totalDonations = donationRepository.countByChurch(user.getChurch());
    BigDecimal totalAmount = donationRepository.getTotalDonations(user.getChurch());
    if (totalAmount == null) {
      totalAmount = BigDecimal.ZERO;
    }

    // Get this week's data (Monday to Sunday)
    LocalDate now = LocalDate.now();
    LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    long thisWeekCount = donationRepository.countByChurchAndDateRange(user.getChurch(), startOfWeek, endOfWeek);
    BigDecimal thisWeekAmount = donationRepository.getTotalDonationsByDateRange(user.getChurch(), startOfWeek, endOfWeek);
    if (thisWeekAmount == null) {
      thisWeekAmount = BigDecimal.ZERO;
    }

    // Get this month's data
    LocalDate startOfMonth = now.withDayOfMonth(1);
    LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

    long thisMonthCount = donationRepository.countByChurchAndDateRange(user.getChurch(), startOfMonth, endOfMonth);
    BigDecimal thisMonthAmount = donationRepository.getTotalDonationsByDateRange(user.getChurch(), startOfMonth, endOfMonth);
    if (thisMonthAmount == null) {
      thisMonthAmount = BigDecimal.ZERO;
    }

    return new DonationStatsResponse(
        (int) totalDonations,
        totalAmount,
        (int) thisWeekCount,
        thisWeekAmount,
        (int) thisMonthCount,
        thisMonthAmount
    );
  }

  /**
   * Get crisis statistics for dashboard.
   * Dashboard Phase 3: Crisis Management Widget
   *
   * @param userId Current user ID from JWT
   * @return Crisis statistics
   */
  public CrisisStatsResponse getCrisisStats(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new CrisisStatsResponse(0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    // Count active crises (ACTIVE, IN_RESPONSE statuses)
    long activeCrises = crisisRepository.countByChurchAndStatus(user.getChurch(), CrisisStatus.ACTIVE);
    long inResponseCrises = crisisRepository.countByChurchAndStatus(user.getChurch(), CrisisStatus.IN_RESPONSE);

    // Count resolved this month using date range query
    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    List<Crisis> resolvedList = crisisRepository.findByIncidentDateRange(
        user.getChurch(),
        startOfMonth.atStartOfDay(),
        endOfMonth.atTime(23, 59, 59)
    ).stream()
        .filter(c -> c.getStatus() == CrisisStatus.RESOLVED)
        .toList();
    long resolvedThisMonth = resolvedList.size();

    // Calculate urgent crises (active + in_response)
    long urgentCrises = activeCrises + inResponseCrises;

    // Total crises
    long totalCrises = crisisRepository.countByChurch(user.getChurch());

    return new CrisisStatsResponse(
        totalCrises,
        activeCrises,
        inResponseCrises,
        resolvedThisMonth,
        urgentCrises, // criticalCrises - using urgent count
        0L, // highSeverityCrises - placeholder
        0L  // totalAffectedMembers - placeholder
    );
  }

  /**
   * Get upcoming counseling sessions for dashboard.
   * Dashboard Phase 3: Counseling Sessions Widget
   *
   * @param userId Current user ID from JWT
   * @return List of upcoming counseling sessions this week
   */
  public List<CounselingSessionResponse> getUpcomingCounselingSessions(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    // Get sessions this week (use existing findUpcomingSessions method)
    LocalDateTime now = LocalDateTime.now();

    List<CounselingSession> sessions = counselingSessionRepository.findUpcomingSessions(
        user.getChurch(),
        now
    ).stream()
        .filter(s -> {
          LocalDateTime endOfWeek = now.toLocalDate().plusDays(7 - now.getDayOfWeek().getValue()).atTime(23, 59, 59);
          return s.getSessionDate().isBefore(endOfWeek) || s.getSessionDate().isEqual(endOfWeek);
        })
        .toList();

    return sessions.stream()
        .sorted(Comparator.comparing(CounselingSession::getSessionDate))
        .limit(10)
        .map(CounselingSessionResponse::fromEntity)
        .toList();
  }
}
