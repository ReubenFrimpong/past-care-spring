package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.AttendanceCertificateRequest;
import com.reuben.pastcare_spring.dtos.AttendanceExportRequest;
import com.reuben.pastcare_spring.dtos.MemberSegmentationResponse;
import com.reuben.pastcare_spring.enums.AttendanceStatus;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.AttendanceRepository;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for exporting attendance data and generating certificates
 */
@Service
@RequiredArgsConstructor
public class AttendanceExportService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final MemberRepository memberRepository;

    /**
     * Export attendance data to Excel format
     */
    public byte[] exportToExcel(AttendanceExportRequest request, Long churchId) throws IOException {
        List<AttendanceSession> sessions = getFilteredSessions(request, churchId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Attendance Report");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "Service Type", "Member Name", "Status", "Check-in Time",
                               "Late?", "Minutes Late", "Fellowship", "Notes"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Populate data rows
            int rowNum = 1;
            for (AttendanceSession session : sessions) {
                List<Attendance> attendances = attendanceRepository.findByAttendanceSession(session);

                for (Attendance attendance : attendances) {
                    // Skip absent members if requested
                    if (!request.includeAbsent() && attendance.getStatus() == AttendanceStatus.ABSENT) {
                        continue;
                    }

                    Row row = sheet.createRow(rowNum++);

                    // Date
                    Cell dateCell = row.createCell(0);
                    dateCell.setCellValue(session.getSessionDate().toString());
                    dateCell.setCellStyle(dateStyle);

                    // Service Type
                    row.createCell(1).setCellValue(session.getServiceType() != null ?
                        session.getServiceType().name() : "N/A");

                    // Member Name
                    row.createCell(2).setCellValue(attendance.getMember().getFirstName() + " " +
                        attendance.getMember().getLastName());

                    // Status
                    row.createCell(3).setCellValue(attendance.getStatus().name());

                    // Check-in Time
                    row.createCell(4).setCellValue(attendance.getCheckInTime() != null ?
                        attendance.getCheckInTime().toString() : "");

                    // Late
                    row.createCell(5).setCellValue(attendance.getIsLate() ? "Yes" : "No");

                    // Minutes Late
                    row.createCell(6).setCellValue(attendance.getMinutesLate() != null ?
                        attendance.getMinutesLate().toString() : "0");

                    // Fellowship
                    row.createCell(7).setCellValue(attendance.getMember().getFellowships() != null &&
                        !attendance.getMember().getFellowships().isEmpty() ?
                        attendance.getMember().getFellowships().get(0).getName() : "");

                    // Notes
                    row.createCell(8).setCellValue(attendance.getRemarks() != null ?
                        attendance.getRemarks() : "");
                }
            }

            // Auto-size all columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Export attendance data to CSV format
     */
    public byte[] exportToCSV(AttendanceExportRequest request, Long churchId) {
        List<AttendanceSession> sessions = getFilteredSessions(request, churchId);
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Date,Service Type,Member Name,Status,Check-in Time,Late?,Minutes Late,Fellowship,Notes\n");

        // Data rows
        for (AttendanceSession session : sessions) {
            List<Attendance> attendances = attendanceRepository.findByAttendanceSession(session);

            for (Attendance attendance : attendances) {
                if (!request.includeAbsent() && attendance.getStatus() == AttendanceStatus.ABSENT) {
                    continue;
                }

                csv.append(session.getSessionDate()).append(",")
                   .append(session.getServiceType() != null ? session.getServiceType().name() : "N/A").append(",")
                   .append("\"").append(attendance.getMember().getFirstName()).append(" ")
                   .append(attendance.getMember().getLastName()).append("\",")
                   .append(attendance.getStatus().name()).append(",")
                   .append(attendance.getCheckInTime() != null ? attendance.getCheckInTime() : "").append(",")
                   .append(attendance.getIsLate() ? "Yes" : "No").append(",")
                   .append(attendance.getMinutesLate() != null ? attendance.getMinutesLate() : 0).append(",")
                   .append(attendance.getMember().getFellowships() != null &&
                          !attendance.getMember().getFellowships().isEmpty() ?
                          attendance.getMember().getFellowships().get(0).getName() : "").append(",")
                   .append("\"").append(attendance.getRemarks() != null ? attendance.getRemarks() : "").append("\"\n");
            }
        }

        return csv.toString().getBytes();
    }

    /**
     * Generate attendance certificate for a member
     */
    public byte[] generateCertificate(AttendanceCertificateRequest request, Long churchId) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new RuntimeException("Member not found"));

        long totalSessions = sessionRepository.countByChurchIdAndSessionDateBetween(
            churchId, request.startDate(), request.endDate());

        long attendedSessions = attendanceRepository.countByMemberIdAndStatusAndSessionDateBetween(
            request.memberId(), AttendanceStatus.PRESENT, request.startDate(), request.endDate());

        System.out.println("Certificate Generation Debug:");
        System.out.println("Church ID: " + churchId);
        System.out.println("Member ID: " + request.memberId());
        System.out.println("Member Name: " + member.getFirstName() + " " + member.getLastName());
        System.out.println("Start Date: " + request.startDate());
        System.out.println("End Date: " + request.endDate());
        System.out.println("Total Sessions in range: " + totalSessions);
        System.out.println("Attended Sessions: " + attendedSessions);

        double attendanceRate = totalSessions > 0 ? (attendedSessions * 100.0 / totalSessions) : 0;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(out);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);

            // Set page size and margins
            pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // Add border
            com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(
                pdfDoc.addNewPage().newContentStreamBefore(), pdfDoc.getFirstPage().getResources(), pdfDoc);
            canvas.setStrokeColor(com.itextpdf.kernel.colors.ColorConstants.BLUE)
                  .setLineWidth(3)
                  .rectangle(30, 30, 535, 782)
                  .stroke();

            // Title
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("CERTIFICATE OF ATTENDANCE")
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD))
                .setFontSize(24)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginTop(80)
                .setMarginBottom(40)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE);
            document.add(title);

            // Certifies text
            com.itextpdf.layout.element.Paragraph certifies = new com.itextpdf.layout.element.Paragraph("This is to certify that")
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA))
                .setFontSize(14)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(certifies);

            // Member name
            com.itextpdf.layout.element.Paragraph memberName = new com.itextpdf.layout.element.Paragraph(
                member.getFirstName() + " " + member.getLastName())
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD))
                .setFontSize(20)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(30)
                .setUnderline();
            document.add(memberName);

            // Details
            String detailsText = String.format("has attended %d out of %d church services\nfrom %s to %s",
                attendedSessions,
                totalSessions,
                request.startDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                request.endDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            com.itextpdf.layout.element.Paragraph details = new com.itextpdf.layout.element.Paragraph(detailsText)
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA))
                .setFontSize(14)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(details);

            // Attendance rate
            com.itextpdf.layout.element.Paragraph rate = new com.itextpdf.layout.element.Paragraph(
                String.format("Attendance Rate: %.1f%%", attendanceRate))
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD))
                .setFontSize(16)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(20)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE);
            document.add(rate);

            // Award text
            String awardText = attendanceRate >= 90 ? "★ EXCELLENT ATTENDANCE ★" :
                              attendanceRate >= 75 ? "★ GOOD ATTENDANCE ★" : "Keep up the commitment!";
            com.itextpdf.layout.element.Paragraph award = new com.itextpdf.layout.element.Paragraph(awardText)
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD))
                .setFontSize(14)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(60)
                .setFontColor(attendanceRate >= 75 ? com.itextpdf.kernel.colors.ColorConstants.GREEN :
                             com.itextpdf.kernel.colors.ColorConstants.ORANGE);
            document.add(award);

            // Generated date
            com.itextpdf.layout.element.Paragraph generatedDate = new com.itextpdf.layout.element.Paragraph(
                "Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA))
                .setFontSize(10)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY);
            document.add(generatedDate);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF certificate", e);
        }
    }

    /**
     * Segment members based on attendance patterns
     */
    public MemberSegmentationResponse segmentMembers(Long churchId, LocalDate startDate, LocalDate endDate) {
        List<Member> members = memberRepository.findByChurchId(churchId);

        List<MemberSegmentationResponse.MemberSegment> activeMembers = new ArrayList<>();
        List<MemberSegmentationResponse.MemberSegment> irregularMembers = new ArrayList<>();
        List<MemberSegmentationResponse.MemberSegment> inactiveMembers = new ArrayList<>();
        List<MemberSegmentationResponse.MemberSegment> atRiskMembers = new ArrayList<>();

        for (Member member : members) {
            long totalSessions = sessionRepository.countByChurchIdAndSessionDateBetween(
                churchId, startDate, endDate);

            if (totalSessions == 0) continue;

            long attendedSessions = attendanceRepository.countByMemberIdAndStatusAndSessionDateBetween(
                member.getId(), AttendanceStatus.PRESENT, startDate, endDate);

            double attendanceRate = (attendedSessions * 100.0 / totalSessions);

            // Get consecutive absences
            int consecutiveAbsences = getConsecutiveAbsences(member.getId(), churchId);

            // Get last attendance date
            String lastAttendanceDate = attendanceRepository
                .findTopByMemberIdAndStatusOrderByCheckInTimeDesc(member.getId(), AttendanceStatus.PRESENT)
                .map(a -> a.getCheckInTime() != null ? a.getCheckInTime().toLocalDate().toString() : "N/A")
                .orElse("Never");

            String segment;
            String recommendation;

            if (attendanceRate >= 75) {
                segment = "ACTIVE";
                recommendation = "Keep up the excellent attendance!";
            } else if (attendanceRate >= 50) {
                segment = "IRREGULAR";
                recommendation = "Encourage more regular attendance";
            } else if (consecutiveAbsences >= 3) {
                segment = "AT_RISK";
                recommendation = "URGENT: Follow up immediately - " + consecutiveAbsences + " consecutive absences";
            } else {
                segment = "INACTIVE";
                recommendation = "Schedule pastoral visit";
            }

            MemberSegmentationResponse.MemberSegment memberSegment =
                new MemberSegmentationResponse.MemberSegment(
                    member.getId(),
                    member.getFirstName() + " " + member.getLastName(),
                    segment,
                    attendanceRate,
                    consecutiveAbsences,
                    (int) totalSessions,
                    (int) attendedSessions,
                    lastAttendanceDate,
                    recommendation
                );

            switch (segment) {
                case "ACTIVE" -> activeMembers.add(memberSegment);
                case "IRREGULAR" -> irregularMembers.add(memberSegment);
                case "AT_RISK" -> atRiskMembers.add(memberSegment);
                default -> inactiveMembers.add(memberSegment);
            }
        }

        // Calculate statistics
        int totalMembers = members.size();
        double avgAttendanceRate = members.isEmpty() ? 0 :
            members.stream()
                .mapToDouble(m -> {
                    long total = sessionRepository.countByChurchIdAndSessionDateBetween(
                        churchId, startDate, endDate);
                    long attended = attendanceRepository.countByMemberIdAndStatusAndSessionDateBetween(
                        m.getId(), AttendanceStatus.PRESENT, startDate, endDate);
                    return total > 0 ? (attended * 100.0 / total) : 0;
                })
                .average()
                .orElse(0);

        MemberSegmentationResponse.SegmentStatistics statistics =
            new MemberSegmentationResponse.SegmentStatistics(
                totalMembers,
                activeMembers.size(),
                irregularMembers.size(),
                inactiveMembers.size(),
                atRiskMembers.size(),
                avgAttendanceRate
            );

        return new MemberSegmentationResponse(
            activeMembers,
            irregularMembers,
            inactiveMembers,
            atRiskMembers,
            statistics
        );
    }

    // Helper methods

    private List<AttendanceSession> getFilteredSessions(AttendanceExportRequest request, Long churchId) {
        // Get all sessions for the church in the date range
        List<AttendanceSession> sessions = sessionRepository
            .findByChurch_IdAndSessionDateBetweenOrderBySessionDateDesc(
                churchId, request.startDate(), request.endDate());

        // Filter by fellowship if specified
        if (request.fellowshipId() != null) {
            sessions = sessions.stream()
                .filter(s -> s.getFellowship() != null && s.getFellowship().getId().equals(request.fellowshipId()))
                .collect(Collectors.toList());
        }

        // Filter by service type if specified
        if (request.serviceType() != null && !request.serviceType().isEmpty()) {
            sessions = sessions.stream()
                .filter(s -> s.getServiceType() != null &&
                           s.getServiceType().name().equals(request.serviceType()))
                .collect(Collectors.toList());
        }

        return sessions;
    }

    private int getConsecutiveAbsences(Long memberId, Long churchId) {
        // Get recent sessions in descending order
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        List<AttendanceSession> recentSessions = sessionRepository
            .findByChurch_IdAndSessionDateBetweenOrderBySessionDateDesc(
                churchId, threeMonthsAgo, LocalDate.now());

        int consecutiveAbsences = 0;
        for (AttendanceSession session : recentSessions) {
            List<Attendance> attendances = attendanceRepository
                .findByAttendanceSessionAndMemberId(session, memberId);

            if (attendances.isEmpty() || attendances.get(0).getStatus() == AttendanceStatus.ABSENT) {
                consecutiveAbsences++;
            } else {
                break; // Stop counting when we find a present attendance
            }
        }

        return consecutiveAbsences;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }
}
