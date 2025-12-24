package com.reuben.pastcare_spring.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.AttendanceAnalyticsResponse;
import com.reuben.pastcare_spring.dtos.AttendanceAnalyticsResponse.*;
import com.reuben.pastcare_spring.dtos.AttendanceSummaryResponse;
import com.reuben.pastcare_spring.dtos.MemberEngagementResponse;
import com.reuben.pastcare_spring.dtos.ServiceTypeAnalyticsResponse;
import com.reuben.pastcare_spring.enums.AttendanceStatus;
import com.reuben.pastcare_spring.enums.CheckInMethod;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.repositories.AttendanceRepository;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.VisitorRepository;

import lombok.RequiredArgsConstructor;

/**
 * Phase 2: Attendance Analytics Service
 * Provides comprehensive analytics and reporting for attendance data
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceAnalyticsService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final VisitorRepository visitorRepository;
    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;

    /**
     * Get comprehensive attendance analytics for a date range
     */
    public AttendanceAnalyticsResponse getAttendanceAnalytics(Long churchId, LocalDate startDate, LocalDate endDate) {
        // Overall statistics
        Long totalSessions = sessionRepository.countSessionsByDateRange(churchId, startDate, endDate);
        Long totalRecords = attendanceRepository.countByChurchAndDateRange(churchId, startDate, endDate);
        Long uniqueMembers = attendanceRepository.countUniqueMembersAttended(churchId, startDate, endDate);

        // Calculate overall attendance rate
        Long presentCount = attendanceRepository.countByChurchDateRangeAndStatus(
            churchId, startDate, endDate, AttendanceStatus.PRESENT);
        Double overallRate = totalRecords > 0 ? (presentCount.doubleValue() / totalRecords) * 100 : 0.0;

        // Trend data
        List<AttendanceTrendPoint> trends = getAttendanceTrends(churchId, startDate, endDate);

        // Service type breakdown
        List<ServiceTypeStats> serviceTypeStats = getServiceTypeStats(churchId, startDate, endDate);

        // Check-in method distribution
        List<CheckInMethodStats> checkInStats = getCheckInMethodStats(churchId, startDate, endDate);

        // Late arrival statistics
        LateArrivalStats lateStats = getLateArrivalStats(churchId, startDate, endDate);

        // Visitor metrics
        VisitorMetrics visitorMetrics = getVisitorMetrics(startDate, endDate);

        return new AttendanceAnalyticsResponse(
            totalSessions,
            totalRecords,
            uniqueMembers,
            overallRate,
            trends,
            serviceTypeStats,
            checkInStats,
            lateStats,
            visitorMetrics,
            startDate,
            endDate
        );
    }

    /**
     * Get attendance summary with key metrics
     */
    public AttendanceSummaryResponse getAttendanceSummary(Long churchId, LocalDate startDate, LocalDate endDate) {
        // Session statistics
        Long totalSessions = sessionRepository.countSessionsByDateRange(churchId, startDate, endDate);
        Map<Boolean, Long> sessionsByStatus = getSessionCompletionStats(churchId, startDate, endDate);
        Long completedSessions = sessionsByStatus.getOrDefault(true, 0L);
        Long upcomingSessions = sessionsByStatus.getOrDefault(false, 0L);

        // Member statistics
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new RuntimeException("Church not found"));
        Long totalMembers = memberRepository.countByChurch(church);
        Long uniqueAttendees = attendanceRepository.countUniqueMembersAttended(churchId, startDate, endDate);

        // Attendance records
        Long totalRecords = attendanceRepository.countByChurchAndDateRange(churchId, startDate, endDate);
        Long presentCount = attendanceRepository.countByChurchDateRangeAndStatus(
            churchId, startDate, endDate, AttendanceStatus.PRESENT);
        Long absentCount = attendanceRepository.countByChurchDateRangeAndStatus(
            churchId, startDate, endDate, AttendanceStatus.ABSENT);
        Long excusedCount = attendanceRepository.countByChurchDateRangeAndStatus(
            churchId, startDate, endDate, AttendanceStatus.EXCUSED);

        Double attendanceRate = totalRecords > 0 ? (presentCount.doubleValue() / totalRecords) * 100 : 0.0;

        // Comparison with previous period
        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(daysDiff);
        LocalDate prevEndDate = startDate.minusDays(1);

        Long prevTotalRecords = attendanceRepository.countByChurchAndDateRange(churchId, prevStartDate, prevEndDate);
        Long prevPresentCount = attendanceRepository.countByChurchDateRangeAndStatus(
            churchId, prevStartDate, prevEndDate, AttendanceStatus.PRESENT);
        Double prevRate = prevTotalRecords > 0 ? (prevPresentCount.doubleValue() / prevTotalRecords) * 100 : 0.0;
        Double rateChange = attendanceRate - prevRate;

        Long prevUniqueAttendees = attendanceRepository.countUniqueMembersAttended(churchId, prevStartDate, prevEndDate);
        Long memberCountChange = uniqueAttendees - prevUniqueAttendees;

        // Check-in method counts
        Map<CheckInMethod, Long> checkInCounts = getCheckInMethodCounts(churchId, startDate, endDate);
        Long qrCodeCheckIns = checkInCounts.getOrDefault(CheckInMethod.QR_CODE, 0L);
        Long geofenceCheckIns = checkInCounts.getOrDefault(CheckInMethod.GEOFENCE, 0L);
        Long manualCheckIns = checkInCounts.getOrDefault(CheckInMethod.MANUAL, 0L);

        // Late arrivals
        List<Object[]> lateStatsList = attendanceRepository.getLateArrivalStats(churchId, startDate, endDate);
        Long lateCheckIns = 0L;
        if (lateStatsList != null && !lateStatsList.isEmpty()) {
            Object[] lateStats = lateStatsList.get(0);
            lateCheckIns = lateStats[0] != null ? ((Number) lateStats[0]).longValue() : 0L;
        }
        Double lateRate = presentCount > 0 ? (lateCheckIns.doubleValue() / presentCount) * 100 : 0.0;

        // Visitor statistics
        Long totalVisitors = visitorRepository.countVisitorsByDateRange(startDate, endDate);
        Long newVisitors = totalVisitors - visitorRepository.countReturningVisitors(startDate, endDate);
        Long returningVisitors = visitorRepository.countReturningVisitors(startDate, endDate);

        return new AttendanceSummaryResponse(
            startDate, endDate,
            totalSessions, completedSessions, upcomingSessions,
            totalMembers, uniqueAttendees, uniqueAttendees,
            totalRecords, presentCount, absentCount, excusedCount, attendanceRate,
            rateChange, memberCountChange,
            qrCodeCheckIns, geofenceCheckIns, manualCheckIns, lateCheckIns, lateRate,
            totalVisitors, newVisitors, returningVisitors
        );
    }

    /**
     * Get member engagement metrics
     */
    public List<MemberEngagementResponse> getMemberEngagement(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> engagementData = attendanceRepository.getMemberEngagementData(churchId, startDate, endDate);
        Long totalSessionsAvailable = sessionRepository.countSessionsByDateRange(churchId, startDate, endDate);

        // Get preferred check-in methods
        Map<Long, CheckInMethod> preferredMethods = getPreferredCheckInMethods(churchId);

        List<MemberEngagementResponse> engagementList = new ArrayList<>();
        for (Object[] data : engagementData) {
            Long memberId = ((Number) data[0]).longValue();
            String memberName = data[1] + " " + data[2];
            Long sessionsAttended = data[3] != null ? ((Number) data[3]).longValue() : 0L;
            Long totalRecords = data[4] != null ? ((Number) data[4]).longValue() : 0L;
            Long timesLate = data[5] != null ? ((Number) data[5]).longValue() : 0L;
            LocalDate lastAttendance = data[6] != null ? (LocalDate) data[6] : null;

            Double attendanceRate = totalSessionsAvailable > 0 ?
                (sessionsAttended.doubleValue() / totalSessionsAvailable) * 100 : 0.0;
            Double lateRate = sessionsAttended > 0 ?
                (timesLate.doubleValue() / sessionsAttended) * 100 : 0.0;

            Long daysSinceLastAttendance = lastAttendance != null ?
                ChronoUnit.DAYS.between(lastAttendance, LocalDate.now()) : null;

            String engagementLevel = MemberEngagementResponse.calculateEngagementLevel(
                attendanceRate, daysSinceLastAttendance);

            String preferredMethod = preferredMethods.get(memberId) != null ?
                preferredMethods.get(memberId).name() : "MANUAL";

            engagementList.add(new MemberEngagementResponse(
                memberId, memberName, totalSessionsAvailable, sessionsAttended,
                attendanceRate, 0L, // consecutive attendance - would need additional query
                timesLate, lateRate, lastAttendance, daysSinceLastAttendance,
                engagementLevel, preferredMethod
            ));
        }

        return engagementList;
    }

    /**
     * Get service type analytics
     */
    public List<ServiceTypeAnalyticsResponse> getServiceTypeAnalytics(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> serviceStats = sessionRepository.getAttendanceStatsByServiceType(churchId, startDate, endDate);

        List<ServiceTypeAnalyticsResponse> analytics = new ArrayList<>();
        for (Object[] stat : serviceStats) {
            String serviceType = stat[0] != null ? stat[0].toString() : "UNKNOWN";
            Long sessionCount = stat[1] != null ? ((Number) stat[1]).longValue() : 0L;
            Long totalAttendance = stat[2] != null ? ((Number) stat[2]).longValue() : 0L;
            Double avgAttendance = stat[3] != null ? ((Number) stat[3]).doubleValue() : 0.0;

            // Calculate attendance rate (would need total member count per session)
            Double attendanceRate = 75.0; // Placeholder - would need more complex calculation

            analytics.add(new ServiceTypeAnalyticsResponse(
                serviceType, sessionCount, totalAttendance, avgAttendance,
                attendanceRate, totalAttendance, 0L, // peak and lowest attendance
                List.of(), List.of() // top attenders and trends - would need additional queries
            ));
        }

        return analytics;
    }

    // Helper methods

    private List<AttendanceTrendPoint> getAttendanceTrends(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> trendsData = attendanceRepository.getAttendanceTrendsByDay(churchId, startDate, endDate);

        return trendsData.stream()
            .map(data -> new AttendanceTrendPoint(
                (LocalDate) data[0],
                ((Number) data[1]).longValue(),
                ((Number) data[2]).longValue(),
                data[1] != null && ((Number) data[1]).longValue() > 0 ?
                    (((Number) data[2]).doubleValue() / ((Number) data[1]).doubleValue()) * 100 : 0.0,
                "ALL" // Service type - would need modification to track by type
            ))
            .collect(Collectors.toList());
    }

    private List<ServiceTypeStats> getServiceTypeStats(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> stats = sessionRepository.getAttendanceStatsByServiceType(churchId, startDate, endDate);

        return stats.stream()
            .map(stat -> new ServiceTypeStats(
                stat[0] != null ? stat[0].toString() : "UNKNOWN",
                stat[1] != null ? ((Number) stat[1]).longValue() : 0L,
                stat[2] != null ? ((Number) stat[2]).longValue() : 0L,
                stat[3] != null ? ((Number) stat[3]).doubleValue() : 0.0,
                75.0 // Placeholder attendance rate
            ))
            .collect(Collectors.toList());
    }

    private List<CheckInMethodStats> getCheckInMethodStats(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> methodCounts = attendanceRepository.countByCheckInMethod(churchId, startDate, endDate);
        Long total = methodCounts.stream()
            .mapToLong(data -> ((Number) data[1]).longValue())
            .sum();

        return methodCounts.stream()
            .map(data -> new CheckInMethodStats(
                data[0] != null ? data[0].toString() : "UNKNOWN",
                ((Number) data[1]).longValue(),
                total > 0 ? (((Number) data[1]).doubleValue() / total) * 100 : 0.0
            ))
            .collect(Collectors.toList());
    }

    private LateArrivalStats getLateArrivalStats(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> statsList = attendanceRepository.getLateArrivalStats(churchId, startDate, endDate);

        Long totalLate = 0L;
        Double avgMinutes = 0.0;
        Long maxMinutes = 0L;

        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0);
            totalLate = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
            avgMinutes = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
            maxMinutes = stats.length > 2 && stats[2] != null ? ((Number) stats[2]).longValue() : 0L;
        }

        Long totalPresent = attendanceRepository.countByChurchDateRangeAndStatus(
            churchId, startDate, endDate, AttendanceStatus.PRESENT);
        Double lateRate = totalPresent > 0 ? (totalLate.doubleValue() / totalPresent) * 100 : 0.0;

        List<Object[]> frequentlyLate = attendanceRepository.findFrequentlyLateMembers(churchId, startDate, endDate);
        List<String> lateMemberNames = frequentlyLate.stream()
            .limit(5)
            .map(data -> data[0] + " " + data[1])
            .collect(Collectors.toList());

        return new LateArrivalStats(totalLate, lateRate, avgMinutes, maxMinutes, lateMemberNames);
    }

    private VisitorMetrics getVisitorMetrics(LocalDate startDate, LocalDate endDate) {
        Long totalVisitors = visitorRepository.countVisitorsByDateRange(startDate, endDate);
        Long returningVisitors = visitorRepository.countReturningVisitors(startDate, endDate);
        Long convertedVisitors = visitorRepository.countConvertedVisitors(startDate, endDate);

        Double conversionRate = totalVisitors > 0 ? (convertedVisitors.doubleValue() / totalVisitors) * 100 : 0.0;
        Double returnRate = totalVisitors > 0 ? (returningVisitors.doubleValue() / totalVisitors) * 100 : 0.0;

        return new VisitorMetrics(totalVisitors, returningVisitors, convertedVisitors, conversionRate, returnRate);
    }

    private Map<Boolean, Long> getSessionCompletionStats(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> stats = sessionRepository.countSessionsByCompletionStatus(churchId, startDate, endDate);
        Map<Boolean, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            Boolean isCompleted = (Boolean) stat[0];
            Long count = ((Number) stat[1]).longValue();
            result.put(isCompleted != null ? isCompleted : false, count);
        }
        return result;
    }

    private Map<CheckInMethod, Long> getCheckInMethodCounts(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> methodCounts = attendanceRepository.countByCheckInMethod(churchId, startDate, endDate);
        Map<CheckInMethod, Long> result = new HashMap<>();
        for (Object[] data : methodCounts) {
            if (data[0] != null) {
                CheckInMethod method = (CheckInMethod) data[0];
                Long count = ((Number) data[1]).longValue();
                result.put(method, count);
            }
        }
        return result;
    }

    private Map<Long, CheckInMethod> getPreferredCheckInMethods(Long churchId) {
        List<Object[]> methods = attendanceRepository.getMemberPreferredCheckInMethods(churchId);
        Map<Long, CheckInMethod> result = new HashMap<>();
        for (Object[] data : methods) {
            Long memberId = ((Number) data[0]).longValue();
            CheckInMethod method = (CheckInMethod) data[1];
            // Only store the most frequent method (first one due to ORDER BY COUNT desc)
            result.putIfAbsent(memberId, method);
        }
        return result;
    }
}
