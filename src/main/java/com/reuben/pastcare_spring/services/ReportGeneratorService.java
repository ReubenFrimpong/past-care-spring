package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.GenerateReportRequest;
import com.reuben.pastcare_spring.enums.ReportType;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating report data for all 13 pre-built report types.
 */
@Service
@RequiredArgsConstructor
public class ReportGeneratorService {

    private final MemberRepository memberRepository;
    private final VisitorRepository visitorRepository;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final CareNeedRepository careNeedRepository;
    private final VisitRepository visitRepository;
    private final FellowshipRepository fellowshipRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final EventRepository eventRepository;
    private final HouseholdRepository householdRepository;

    private final CsvReportService csvReportService;
    private final ExcelReportService excelReportService;
    private final PdfReportService pdfReportService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Main entry point for generating report data.
     */
    public List<List<Object>> generateReportData(ReportType reportType, GenerateReportRequest request, Long churchId) {
        return switch (reportType) {
            case MEMBER_DIRECTORY -> getMemberDirectoryRows(churchId);
            case BIRTHDAY_ANNIVERSARY_LIST -> getBirthdayAnniversaryRows(churchId, request.getStartDate(), request.getEndDate());
            case INACTIVE_MEMBERS -> getInactiveMembersRows(churchId, request.getStartDate());
            case HOUSEHOLD_ROSTER -> getHouseholdRosterRows(churchId);
            case ATTENDANCE_SUMMARY -> getAttendanceSummaryRows(churchId, request.getStartDate(), request.getEndDate());
            case FIRST_TIME_VISITORS -> getFirstTimeVisitorsRows(churchId, request.getStartDate(), request.getEndDate());
            case GIVING_SUMMARY -> getGivingSummaryRows(churchId, request.getStartDate(), request.getEndDate());
            case TOP_DONORS -> getTopDonorsRows(churchId, request.getStartDate(), request.getEndDate());
            case CAMPAIGN_PROGRESS -> getCampaignProgressRows(churchId);
            case FELLOWSHIP_ROSTER -> getFellowshipRosterRows(churchId);
            case PASTORAL_CARE_SUMMARY -> getPastoralCareSummaryRows(churchId, request.getStartDate(), request.getEndDate());
            case EVENT_ATTENDANCE -> getEventAttendanceRows(churchId, request.getStartDate(), request.getEndDate());
            case GROWTH_TREND -> getGrowthTrendRows(churchId, request.getStartDate(), request.getEndDate());
        };
    }

    /**
     * Get headers for each report type.
     */
    public List<String> getReportHeaders(ReportType reportType) {
        return switch (reportType) {
            case MEMBER_DIRECTORY -> Arrays.asList("Name", "Phone", "Email", "Status", "Member Since", "Location");
            case BIRTHDAY_ANNIVERSARY_LIST -> Arrays.asList("Name", "Type", "Date", "Age/Years", "Phone", "Email");
            case INACTIVE_MEMBERS -> Arrays.asList("Name", "Phone", "Email", "Last Visit Date", "Days Inactive", "Status");
            case HOUSEHOLD_ROSTER -> Arrays.asList("Household Name", "Head", "Members Count", "Location", "Contact");
            case ATTENDANCE_SUMMARY -> Arrays.asList("Session Date", "Total Attendees", "Members", "Visitors", "Children");
            case FIRST_TIME_VISITORS -> Arrays.asList("Name", "Phone", "Email", "Visit Date", "Age Group", "Invited By");
            case GIVING_SUMMARY -> Arrays.asList("Donor", "Total Amount", "Donation Count", "Average Donation", "Last Donation");
            case TOP_DONORS -> Arrays.asList("Rank", "Donor Name", "Total Amount", "Donation Count", "Largest Gift");
            case CAMPAIGN_PROGRESS -> Arrays.asList("Campaign", "Goal Amount", "Raised Amount", "Progress %", "Donor Count", "Status");
            case FELLOWSHIP_ROSTER -> Arrays.asList("Fellowship Name", "Leader", "Members Count", "Meeting Day", "Location");
            case PASTORAL_CARE_SUMMARY -> Arrays.asList("Member", "Visit Type", "Visit Date", "Care Need", "Follow-up Required", "Notes");
            case EVENT_ATTENDANCE -> Arrays.asList("Event", "Date", "Total Registered", "Total Attended", "Attendance %", "No-Shows");
            case GROWTH_TREND -> Arrays.asList("Month", "New Members", "Visitors", "Avg Attendance", "Total Donations", "Growth %");
        };
    }

    // ========== REPORT GENERATOR METHODS ==========

    private List<List<Object>> getMemberDirectoryRows(Long churchId) {
        List<Member> members = memberRepository.findByChurchId(churchId);

        return members.stream()
                .map(member -> {
                    List<Object> row = new ArrayList<>();
                    row.add(member.getFirstName() + " " + member.getLastName());
                    row.add(member.getPhoneNumber() != null ? member.getPhoneNumber() : "");
                    row.add(member.getEmail() != null ? member.getEmail() : "");
                    row.add(member.getStatus() != null ? member.getStatus().toString() : "");
                    row.add(member.getMemberSince() != null ? member.getMemberSince().toString() : "");
                    row.add(member.getLocation() != null ? member.getLocation().getDisplayName() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getBirthdayAnniversaryRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Member> members = memberRepository.findByChurchId(churchId);
        List<List<Object>> rows = new ArrayList<>();

        LocalDate today = LocalDate.now();
        if (startDate == null) startDate = today;
        if (endDate == null) endDate = today.plusMonths(1);

        for (Member member : members) {
            if (member.getDob() != null) {
                LocalDate nextBirthday = member.getDob().withYear(today.getYear());
                if (nextBirthday.isBefore(today)) {
                    nextBirthday = nextBirthday.plusYears(1);
                }
                if (!nextBirthday.isBefore(startDate) && !nextBirthday.isAfter(endDate)) {
                    List<Object> row = new ArrayList<>();
                    row.add(member.getFirstName() + " " + member.getLastName());
                    row.add("Birthday");
                    row.add(nextBirthday);
                    row.add(ChronoUnit.YEARS.between(member.getDob(), nextBirthday) + " years");
                    row.add(member.getPhoneNumber() != null ? member.getPhoneNumber() : "");
                    row.add(member.getEmail() != null ? member.getEmail() : "");
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    private List<List<Object>> getInactiveMembersRows(Long churchId, LocalDate inactiveSince) {
        List<Member> members = memberRepository.findByChurchId(churchId);

        if (inactiveSince == null) {
            inactiveSince = LocalDate.now().minusMonths(3);
        }

        return members.stream()
                .map(member -> {
                    List<Object> row = new ArrayList<>();
                    row.add(member.getFirstName() + " " + member.getLastName());
                    row.add(member.getPhoneNumber() != null ? member.getPhoneNumber() : "");
                    row.add(member.getEmail() != null ? member.getEmail() : "");
                    row.add("N/A");
                    row.add("N/A");
                    row.add(member.getStatus() != null ? member.getStatus().toString() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getHouseholdRosterRows(Long churchId) {
        List<Household> households = householdRepository.findByChurch_Id(churchId);

        return households.stream()
                .map(household -> {
                    List<Object> row = new ArrayList<>();
                    row.add(household.getHouseholdName() != null ? household.getHouseholdName() : "");
                    row.add(household.getHouseholdHead() != null ? household.getHouseholdHead().getFirstName() + " " + household.getHouseholdHead().getLastName() : "");
                    row.add(household.getMembers() != null ? household.getMembers().size() : 0);
                    row.add(household.getSharedLocation() != null ? household.getSharedLocation().getDisplayName() : "");
                    row.add(household.getHouseholdPhone() != null ? household.getHouseholdPhone() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getAttendanceSummaryRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        List<AttendanceSession> sessions = attendanceSessionRepository.findByChurch_IdAndSessionDateBetween(
                churchId, startDate, endDate);

        return sessions.stream()
                .map(session -> {
                    List<Object> row = new ArrayList<>();
                    row.add(session.getSessionDate());
                    row.add(session.getAttendances() != null ? session.getAttendances().size() : 0);
                    row.add("N/A");
                    row.add("N/A");
                    row.add("N/A");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getFirstTimeVisitorsRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Visitor> visitors = visitorRepository.findByChurch_Id(churchId);

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        return visitors.stream()
                .filter(v -> {
                    LocalDate visitDate = v.getLastVisitDate();
                    return visitDate != null && !visitDate.isBefore(finalStartDate) && !visitDate.isAfter(finalEndDate);
                })
                .map(visitor -> {
                    List<Object> row = new ArrayList<>();
                    row.add(visitor.getFirstName() + " " + visitor.getLastName());
                    row.add(visitor.getPhoneNumber() != null ? visitor.getPhoneNumber() : "");
                    row.add(visitor.getEmail() != null ? visitor.getEmail() : "");
                    row.add(visitor.getLastVisitDate());
                    row.add(visitor.getAgeGroup() != null ? visitor.getAgeGroup().toString() : "");
                    row.add(visitor.getInvitedByMember() != null ? visitor.getInvitedByMember().getFirstName() + " " + visitor.getInvitedByMember().getLastName() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getGivingSummaryRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Donation> donations = donationRepository.findByChurch_Id(churchId);

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        return donations.stream()
                .filter(d -> {
                    LocalDate donationDate = d.getDonationDate();
                    return donationDate != null && !donationDate.isBefore(finalStartDate) && !donationDate.isAfter(finalEndDate);
                })
                .collect(Collectors.groupingBy(d -> d.getMember()))
                .entrySet().stream()
                .map(entry -> {
                    Member member = entry.getKey();
                    List<Donation> memberDonations = entry.getValue();

                    BigDecimal total = memberDonations.stream()
                            .map(Donation::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal average = total.divide(BigDecimal.valueOf(memberDonations.size()), 2, RoundingMode.HALF_UP);

                    LocalDate lastDonation = memberDonations.stream()
                            .map(Donation::getDonationDate)
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    List<Object> row = new ArrayList<>();
                    row.add(member != null ? member.getFirstName() + " " + member.getLastName() : "Anonymous");
                    row.add(total);
                    row.add(memberDonations.size());
                    row.add(average);
                    row.add(lastDonation);
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getTopDonorsRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Donation> donations = donationRepository.findByChurch_Id(churchId);

        if (startDate == null) startDate = LocalDate.now().minusYears(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        List<List<Object>> rows = donations.stream()
                .filter(d -> {
                    LocalDate donationDate = d.getDonationDate();
                    return donationDate != null && !donationDate.isBefore(finalStartDate) && !donationDate.isAfter(finalEndDate);
                })
                .collect(Collectors.groupingBy(d -> d.getMember()))
                .entrySet().stream()
                .map(entry -> {
                    Member member = entry.getKey();
                    List<Donation> memberDonations = entry.getValue();

                    BigDecimal total = memberDonations.stream()
                            .map(Donation::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal largest = memberDonations.stream()
                            .map(Donation::getAmount)
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

                    List<Object> row = new ArrayList<>();
                    row.add(0);
                    row.add(member != null ? member.getFirstName() + " " + member.getLastName() : "Anonymous");
                    row.add(total);
                    row.add(memberDonations.size());
                    row.add(largest);
                    return row;
                })
                .sorted((a, b) -> ((BigDecimal)b.get(2)).compareTo((BigDecimal)a.get(2)))
                .limit(50)
                .collect(Collectors.toList());

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).set(0, i + 1);
        }

        return rows;
    }

    private List<List<Object>> getCampaignProgressRows(Long churchId) {
        List<Campaign> campaigns = campaignRepository.findByChurch_Id(churchId);

        return campaigns.stream()
                .map(campaign -> {
                    List<Object> row = new ArrayList<>();
                    row.add(campaign.getName() != null ? campaign.getName() : "");
                    row.add(campaign.getGoalAmount() != null ? campaign.getGoalAmount() : BigDecimal.ZERO);
                    row.add(campaign.getCurrentAmount() != null ? campaign.getCurrentAmount() : BigDecimal.ZERO);

                    BigDecimal progress = BigDecimal.ZERO;
                    if (campaign.getGoalAmount() != null && campaign.getGoalAmount().compareTo(BigDecimal.ZERO) > 0) {
                        progress = campaign.getCurrentAmount()
                                .divide(campaign.getGoalAmount(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                    }
                    row.add(progress.setScale(2, RoundingMode.HALF_UP) + "%");
                    row.add("N/A");
                    row.add(campaign.getStatus() != null ? campaign.getStatus().toString() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getFellowshipRosterRows(Long churchId) {
        List<Fellowship> fellowships = fellowshipRepository.findByChurch_Id(churchId);

        return fellowships.stream()
                .map(fellowship -> {
                    List<Object> row = new ArrayList<>();
                    row.add(fellowship.getName() != null ? fellowship.getName() : "");
                    row.add(fellowship.getLeader() != null ? fellowship.getLeader().getName() : "");
                    row.add(fellowship.getMembers() != null ? fellowship.getMembers().size() : 0);
                    row.add(fellowship.getMeetingDay() != null ? fellowship.getMeetingDay().toString() : "");
                    row.add(fellowship.getMeetingLocation() != null ? fellowship.getMeetingLocation() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getPastoralCareSummaryRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Visit> visits = visitRepository.findByChurch_Id(churchId);

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        return visits.stream()
                .filter(visit -> {
                    LocalDate visitDate = visit.getVisitDate();
                    return visitDate != null && !visitDate.isBefore(finalStartDate) && !visitDate.isAfter(finalEndDate);
                })
                .map(visit -> {
                    List<Object> row = new ArrayList<>();
                    row.add(visit.getMember() != null ? visit.getMember().getFirstName() + " " + visit.getMember().getLastName() : "");
                    row.add(visit.getType() != null ? visit.getType().toString() : "");
                    row.add(visit.getVisitDate());
                    row.add(visit.getCareNeed() != null ? visit.getCareNeed().getType().toString() : "");
                    row.add(visit.getFollowUpRequired() != null && visit.getFollowUpRequired() ? "Yes" : "No");
                    row.add(visit.getNotes() != null ? visit.getNotes() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getEventAttendanceRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Event> events = eventRepository.findByChurchIdAndDeletedAtIsNull(churchId);

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        return events.stream()
                .filter(event -> {
                    LocalDate eventDate = event.getStartDate().toLocalDate();
                    return eventDate != null && !eventDate.isBefore(finalStartDate) && !eventDate.isAfter(finalEndDate);
                })
                .map(event -> {
                    List<Object> row = new ArrayList<>();
                    row.add(event.getName() != null ? event.getName() : "");
                    row.add(event.getStartDate());

                    int registered = event.getRegistrations() != null ? event.getRegistrations().size() : 0;
                    int attended = event.getRegistrations() != null ?
                            (int) event.getRegistrations().stream().filter(r -> r.getStatus().toString().equals("ATTENDED")).count() : 0;

                    row.add(registered);
                    row.add(attended);

                    double attendancePercent = registered > 0 ? (attended * 100.0 / registered) : 0;
                    row.add(String.format("%.1f%%", attendancePercent));
                    row.add(registered - attended);
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> getGrowthTrendRows(Long churchId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusYears(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Member> members = memberRepository.findByChurchId(churchId);
        List<Visitor> visitors = visitorRepository.findByChurch_Id(churchId);
        List<Donation> donations = donationRepository.findByChurch_Id(churchId);

        List<List<Object>> rows = new ArrayList<>();

        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            LocalDate monthStart = current;
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);

            long newMembers = members.stream()
                    .filter(m -> m.getMemberSince() != null &&
                            m.getMemberSince().getYear() == monthStart.getYear() &&
                            m.getMemberSince().getMonthValue() == monthStart.getMonthValue())
                    .count();

            long monthVisitors = visitors.stream()
                    .filter(v -> v.getLastVisitDate() != null &&
                            !v.getLastVisitDate().isBefore(monthStart) &&
                            !v.getLastVisitDate().isAfter(monthEnd))
                    .count();

            BigDecimal monthDonations = donations.stream()
                    .filter(d -> d.getDonationDate() != null &&
                            !d.getDonationDate().isBefore(monthStart) &&
                            !d.getDonationDate().isAfter(monthEnd))
                    .map(Donation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Object> row = new ArrayList<>();
            row.add(current.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            row.add(newMembers);
            row.add(monthVisitors);
            row.add("N/A");
            row.add(monthDonations);
            row.add("N/A");
            rows.add(row);

            current = current.plusMonths(1);
        }

        return rows;
    }

    /**
     * Generate report and return as byte array in specified format.
     * This is the main method called by ReportService.
     */
    public byte[] generateReport(GenerateReportRequest request, Long churchId) throws java.io.IOException {
        ReportType reportType = request.getReportType();
        
        // Get headers and data
        List<String> headers = getReportHeaders(reportType);
        List<List<Object>> rows = generateReportData(reportType, request, churchId);
        
        // Generate in requested format
        return switch (request.getFormat()) {
            case CSV -> csvReportService.generateCsvReport(headers, rows);
            case EXCEL -> excelReportService.generateExcelReport(headers, rows, reportType.getDisplayName());
            case PDF -> pdfReportService.generatePdfReport(reportType.getDisplayName(), headers, rows);
        };
    }
}
