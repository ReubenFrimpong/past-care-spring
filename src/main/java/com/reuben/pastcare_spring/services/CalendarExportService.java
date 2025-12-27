package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventLocationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service for exporting events to calendar formats (iCal, Google Calendar)
 */
@Service
@Slf4j
public class CalendarExportService {

    private static final DateTimeFormatter ICAL_DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Generate iCal format for a single event
     */
    public String generateICalForEvent(Event event) {
        StringBuilder ical = new StringBuilder();

        // iCal header
        ical.append("BEGIN:VCALENDAR\r\n");
        ical.append("VERSION:2.0\r\n");
        ical.append("PRODID:-//PastCare//Event Management System//EN\r\n");
        ical.append("CALSCALE:GREGORIAN\r\n");
        ical.append("METHOD:PUBLISH\r\n");

        // Add event
        appendEvent(ical, event);

        // iCal footer
        ical.append("END:VCALENDAR\r\n");

        return ical.toString();
    }

    /**
     * Generate iCal format for multiple events
     */
    public String generateICalForEvents(List<Event> events, String calendarName) {
        StringBuilder ical = new StringBuilder();

        // iCal header
        ical.append("BEGIN:VCALENDAR\r\n");
        ical.append("VERSION:2.0\r\n");
        ical.append("PRODID:-//PastCare//Event Management System//EN\r\n");
        ical.append("CALSCALE:GREGORIAN\r\n");
        ical.append("METHOD:PUBLISH\r\n");
        ical.append("X-WR-CALNAME:").append(escapeText(calendarName)).append("\r\n");
        ical.append("X-WR-TIMEZONE:").append(ZoneId.systemDefault().getId()).append("\r\n");

        // Add all events
        for (Event event : events) {
            appendEvent(ical, event);
        }

        // iCal footer
        ical.append("END:VCALENDAR\r\n");

        return ical.toString();
    }

    /**
     * Append a single event to the iCal builder
     */
    private void appendEvent(StringBuilder ical, Event event) {
        ical.append("BEGIN:VEVENT\r\n");

        // UID - unique identifier
        String uid = event.getId() + "@pastcare.app";
        ical.append("UID:").append(uid).append("\r\n");

        // DTSTAMP - timestamp when event was created
        LocalDateTime now = LocalDateTime.now();
        ical.append("DTSTAMP:").append(formatDateTime(now)).append("\r\n");

        // DTSTART - event start date/time
        ical.append("DTSTART:").append(formatDateTime(event.getStartDate())).append("\r\n");

        // DTEND - event end date/time
        ical.append("DTEND:").append(formatDateTime(event.getEndDate())).append("\r\n");

        // SUMMARY - event name
        ical.append("SUMMARY:").append(escapeText(event.getName())).append("\r\n");

        // DESCRIPTION - event description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            ical.append("DESCRIPTION:").append(escapeText(event.getDescription())).append("\r\n");
        }

        // LOCATION - physical or virtual location
        String location = getEventLocation(event);
        if (location != null && !location.isEmpty()) {
            ical.append("LOCATION:").append(escapeText(location)).append("\r\n");
        }

        // ORGANIZER - church/organizer info
        if (event.getChurch() != null) {
            ical.append("ORGANIZER;CN=").append(escapeText(event.getChurch().getName()))
                .append(":mailto:info@").append(event.getChurch().getName().toLowerCase().replace(" ", ""))
                .append(".church\r\n");
        }

        // STATUS
        if (event.getIsCancelled()) {
            ical.append("STATUS:CANCELLED\r\n");
        } else {
            ical.append("STATUS:CONFIRMED\r\n");
        }

        // TRANSP - free/busy time
        ical.append("TRANSP:OPAQUE\r\n");

        // SEQUENCE - version number (for updates)
        ical.append("SEQUENCE:0\r\n");

        // URL - event details URL
        if (event.getVirtualLink() != null && !event.getVirtualLink().isEmpty()) {
            ical.append("URL:").append(event.getVirtualLink()).append("\r\n");
        }

        // Categories/Tags
        if (event.getEventType() != null) {
            ical.append("CATEGORIES:").append(event.getEventType().name()).append("\r\n");
        }

        ical.append("END:VEVENT\r\n");
    }

    /**
     * Get event location based on location type
     */
    private String getEventLocation(Event event) {
        if (event.getLocationType() == EventLocationType.PHYSICAL) {
            return event.getPhysicalLocation();
        } else if (event.getLocationType() == EventLocationType.VIRTUAL) {
            return "Virtual: " + (event.getVirtualPlatform() != null ? event.getVirtualPlatform() : "Online");
        } else if (event.getLocationType() == EventLocationType.HYBRID) {
            String location = "";
            if (event.getPhysicalLocation() != null) {
                location += event.getPhysicalLocation();
            }
            if (event.getVirtualPlatform() != null) {
                if (!location.isEmpty()) location += " / ";
                location += "Virtual: " + event.getVirtualPlatform();
            }
            return location;
        }
        return null;
    }

    /**
     * Format date/time for iCal format
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(ICAL_DATE_FORMAT);
    }

    /**
     * Escape special characters in iCal text fields
     */
    private String escapeText(String text) {
        if (text == null) return "";

        return text
            .replace("\\", "\\\\")  // Escape backslash
            .replace(";", "\\;")     // Escape semicolon
            .replace(",", "\\,")     // Escape comma
            .replace("\n", "\\n")    // Escape newline
            .replace("\r", "");      // Remove carriage return
    }

    /**
     * Generate Google Calendar URL for an event
     */
    public String generateGoogleCalendarUrl(Event event) {
        StringBuilder url = new StringBuilder("https://calendar.google.com/calendar/render?action=TEMPLATE");

        // Event title
        url.append("&text=").append(urlEncode(event.getName()));

        // Event dates
        String dates = formatDateTimeForGoogle(event.getStartDate()) + "/" +
                      formatDateTimeForGoogle(event.getEndDate());
        url.append("&dates=").append(dates);

        // Event description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            url.append("&details=").append(urlEncode(event.getDescription()));
        }

        // Event location
        String location = getEventLocation(event);
        if (location != null && !location.isEmpty()) {
            url.append("&location=").append(urlEncode(location));
        }

        // Add virtual link to description if available
        if (event.getVirtualLink() != null && !event.getVirtualLink().isEmpty()) {
            String virtualInfo = "\n\nJoin virtually: " + event.getVirtualLink();
            url.append("&details=").append(urlEncode(virtualInfo));
        }

        return url.toString();
    }

    /**
     * Format date/time for Google Calendar URL
     */
    private String formatDateTimeForGoogle(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        return dateTime.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of("UTC"))
            .format(formatter);
    }

    /**
     * URL encode text for Google Calendar URL
     */
    private String urlEncode(String text) {
        if (text == null) return "";

        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            log.error("Error encoding URL", e);
            return text;
        }
    }

    /**
     * Generate public calendar embed HTML
     */
    public String generateEmbedCode(Long churchId, int width, int height) {
        String embedUrl = "/api/public/calendar/embed/" + churchId;

        StringBuilder html = new StringBuilder();
        html.append("<iframe ");
        html.append("src=\"").append(embedUrl).append("\" ");
        html.append("width=\"").append(width).append("\" ");
        html.append("height=\"").append(height).append("\" ");
        html.append("frameborder=\"0\" ");
        html.append("scrolling=\"no\" ");
        html.append("style=\"border: 1px solid #ccc; border-radius: 8px;\">");
        html.append("</iframe>");

        return html.toString();
    }
}
