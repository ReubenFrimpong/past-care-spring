package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.AttendanceCertificateRequest;
import com.reuben.pastcare_spring.dtos.AttendanceExportRequest;
import com.reuben.pastcare_spring.dtos.MemberSegmentationResponse;
import com.reuben.pastcare_spring.services.AttendanceExportService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

/**
 * REST Controller for attendance export and reporting features.
 * Phase 4: Integration & Reporting
 */
@RestController
@RequestMapping("/api/attendance/export")
@RequiredArgsConstructor
@Tag(name = "Attendance Export", description = "Export attendance data and generate reports")
public class AttendanceExportController {

    private final AttendanceExportService exportService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Export attendance data to Excel format
     */
    @PostMapping("/excel")
    @Operation(summary = "Export attendance to Excel",
               description = "Export attendance data for a date range to Excel format")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestBody AttendanceExportRequest request,
            HttpServletRequest httpRequest) {

        try {
            Long churchId = requestContextUtil.extractChurchId(httpRequest);
            byte[] excelData = exportService.exportToExcel(request, churchId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                "attendance-report-" + LocalDate.now() + ".xlsx");

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export attendance data to CSV format
     */
    @PostMapping("/csv")
    @Operation(summary = "Export attendance to CSV",
               description = "Export attendance data for a date range to CSV format")
    public ResponseEntity<byte[]> exportToCsv(
            @RequestBody AttendanceExportRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        byte[] csvData = exportService.exportToCSV(request, churchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
            "attendance-report-" + LocalDate.now() + ".csv");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }

    /**
     * Generate attendance certificate for a member
     */
    @PostMapping("/certificate")
    @Operation(summary = "Generate attendance certificate",
               description = "Generate a certificate for a member's attendance")
    public ResponseEntity<byte[]> generateCertificate(
            @RequestBody AttendanceCertificateRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        byte[] certificateData = exportService.generateCertificate(request, churchId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
            "attendance-certificate-" + request.memberId() + ".pdf");

        return new ResponseEntity<>(certificateData, headers, HttpStatus.OK);
    }

    /**
     * Get member segmentation based on attendance patterns
     */
    @GetMapping("/segmentation")
    @Operation(summary = "Get member segmentation",
               description = "Analyze and segment members by attendance patterns")
    public ResponseEntity<MemberSegmentationResponse> getMemberSegmentation(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        MemberSegmentationResponse segmentation = exportService.segmentMembers(
            churchId, startDate, endDate);

        return ResponseEntity.ok(segmentation);
    }
}
