package com.memgres.functions;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Date/Time functions for MemGres database.
 * Provides PostgreSQL-compatible date and time functions.
 */
public class DateTimeFunctions {
    
    /**
     * Get the current timestamp (equivalent to PostgreSQL's NOW()).
     * @return the current timestamp with timezone
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
    
    /**
     * Get the current date (equivalent to PostgreSQL's CURRENT_DATE).
     * @return the current date
     */
    public static LocalDate currentDate() {
        return LocalDate.now(ZoneOffset.UTC);
    }
    
    /**
     * Get the current time (equivalent to PostgreSQL's CURRENT_TIME).
     * @return the current time with timezone
     */
    public static OffsetTime currentTime() {
        return OffsetTime.now(ZoneOffset.UTC);
    }
    
    /**
     * Get the current timestamp (equivalent to PostgreSQL's CURRENT_TIMESTAMP).
     * @return the current timestamp with timezone
     */
    public static ZonedDateTime currentTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
    
    /**
     * Extract a field from a date/time value (equivalent to PostgreSQL's EXTRACT()).
     * @param field the field to extract (year, month, day, hour, minute, second, etc.)
     * @param datetime the date/time value to extract from
     * @return the extracted field value
     */
    public static Double extract(String field, TemporalAccessor datetime) {
        if (field == null || datetime == null) {
            return null;
        }
        
        String normalizedField = field.toLowerCase().trim();
        
        switch (normalizedField) {
            case "year":
                return (double) datetime.get(ChronoField.YEAR);
            case "month":
                return (double) datetime.get(ChronoField.MONTH_OF_YEAR);
            case "day":
                return (double) datetime.get(ChronoField.DAY_OF_MONTH);
            case "hour":
                return (double) datetime.get(ChronoField.HOUR_OF_DAY);
            case "minute":
                return (double) datetime.get(ChronoField.MINUTE_OF_HOUR);
            case "second":
                if (datetime.isSupported(ChronoField.NANO_OF_SECOND)) {
                    double seconds = datetime.get(ChronoField.SECOND_OF_MINUTE);
                    double nanos = datetime.get(ChronoField.NANO_OF_SECOND);
                    return seconds + (nanos / 1_000_000_000.0);
                } else {
                    return (double) datetime.get(ChronoField.SECOND_OF_MINUTE);
                }
            case "millisecond":
                if (datetime.isSupported(ChronoField.MILLI_OF_SECOND)) {
                    return (double) datetime.get(ChronoField.MILLI_OF_SECOND);
                } else if (datetime.isSupported(ChronoField.NANO_OF_SECOND)) {
                    return datetime.get(ChronoField.NANO_OF_SECOND) / 1_000_000.0;
                } else {
                    return 0.0;
                }
            case "microsecond":
                if (datetime.isSupported(ChronoField.MICRO_OF_SECOND)) {
                    return (double) datetime.get(ChronoField.MICRO_OF_SECOND);
                } else if (datetime.isSupported(ChronoField.NANO_OF_SECOND)) {
                    return datetime.get(ChronoField.NANO_OF_SECOND) / 1_000.0;
                } else {
                    return 0.0;
                }
            case "nanosecond":
                if (datetime.isSupported(ChronoField.NANO_OF_SECOND)) {
                    return (double) datetime.get(ChronoField.NANO_OF_SECOND);
                } else {
                    return 0.0;
                }
            case "quarter":
                int month = datetime.get(ChronoField.MONTH_OF_YEAR);
                return (double) ((month - 1) / 3 + 1);
            case "week":
                if (datetime.isSupported(ChronoField.ALIGNED_WEEK_OF_YEAR)) {
                    return (double) datetime.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                } else {
                    return null;
                }
            case "dow": case "dayofweek":
                if (datetime.isSupported(ChronoField.DAY_OF_WEEK)) {
                    // PostgreSQL: Sunday=0, Monday=1, ... Saturday=6
                    int isoDayOfWeek = datetime.get(ChronoField.DAY_OF_WEEK); // Monday=1, Sunday=7
                    return (double) (isoDayOfWeek == 7 ? 0 : isoDayOfWeek);
                } else {
                    return null;
                }
            case "doy": case "dayofyear":
                if (datetime.isSupported(ChronoField.DAY_OF_YEAR)) {
                    return (double) datetime.get(ChronoField.DAY_OF_YEAR);
                } else {
                    return null;
                }
            case "epoch":
                if (datetime instanceof ZonedDateTime) {
                    return (double) ((ZonedDateTime) datetime).toEpochSecond();
                } else if (datetime instanceof OffsetDateTime) {
                    return (double) ((OffsetDateTime) datetime).toEpochSecond();
                } else if (datetime instanceof LocalDateTime) {
                    return (double) ((LocalDateTime) datetime).toEpochSecond(ZoneOffset.UTC);
                } else if (datetime instanceof LocalDate) {
                    return (double) ((LocalDate) datetime).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
                } else {
                    return null;
                }
            default:
                throw new IllegalArgumentException("Unsupported extract field: " + field);
        }
    }
    
    /**
     * Add an interval to a date/time value.
     * @param datetime the base date/time
     * @param interval the interval to add (e.g., "1 day", "2 hours", "3 months")
     * @return the resulting date/time
     */
    public static TemporalAccessor dateAdd(TemporalAccessor datetime, String interval) {
        if (datetime == null || interval == null) {
            return null;
        }
        
        // Simple interval parsing - can be enhanced later
        String[] parts = interval.trim().toLowerCase().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid interval format: " + interval);
        }
        
        long amount;
        try {
            amount = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid interval amount: " + parts[0]);
        }
        
        String unit = parts[1];
        
        if (datetime instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) datetime;
            switch (unit) {
                case "year": case "years":
                    return ldt.plusYears(amount);
                case "month": case "months":
                    return ldt.plusMonths(amount);
                case "day": case "days":
                    return ldt.plusDays(amount);
                case "hour": case "hours":
                    return ldt.plusHours(amount);
                case "minute": case "minutes":
                    return ldt.plusMinutes(amount);
                case "second": case "seconds":
                    return ldt.plusSeconds(amount);
                default:
                    throw new IllegalArgumentException("Unsupported interval unit: " + unit);
            }
        } else if (datetime instanceof LocalDate) {
            LocalDate ld = (LocalDate) datetime;
            switch (unit) {
                case "year": case "years":
                    return ld.plusYears(amount);
                case "month": case "months":
                    return ld.plusMonths(amount);
                case "day": case "days":
                    return ld.plusDays(amount);
                default:
                    throw new IllegalArgumentException("Cannot add time units to date: " + unit);
            }
        } else if (datetime instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) datetime;
            switch (unit) {
                case "year": case "years":
                    return zdt.plusYears(amount);
                case "month": case "months":
                    return zdt.plusMonths(amount);
                case "day": case "days":
                    return zdt.plusDays(amount);
                case "hour": case "hours":
                    return zdt.plusHours(amount);
                case "minute": case "minutes":
                    return zdt.plusMinutes(amount);
                case "second": case "seconds":
                    return zdt.plusSeconds(amount);
                default:
                    throw new IllegalArgumentException("Unsupported interval unit: " + unit);
            }
        }
        
        throw new IllegalArgumentException("Unsupported datetime type: " + datetime.getClass());
    }
    
    /**
     * Format a date/time value as a string.
     * @param datetime the date/time to format
     * @param pattern the format pattern (PostgreSQL-style or Java DateTimeFormatter pattern)
     * @return the formatted string
     */
    public static String formatDateTime(TemporalAccessor datetime, String pattern) {
        if (datetime == null || pattern == null) {
            return null;
        }
        
        // Convert some common PostgreSQL patterns to Java patterns
        String javaPattern = pattern
            .replace("YYYY", "yyyy")
            .replace("MM", "MM")
            .replace("DD", "dd")
            .replace("HH24", "HH")
            .replace("MI", "mm")
            .replace("SS", "ss");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaPattern);
        
        if (datetime instanceof LocalDateTime) {
            return ((LocalDateTime) datetime).format(formatter);
        } else if (datetime instanceof LocalDate) {
            return ((LocalDate) datetime).format(formatter);
        } else if (datetime instanceof LocalTime) {
            return ((LocalTime) datetime).format(formatter);
        } else if (datetime instanceof ZonedDateTime) {
            return ((ZonedDateTime) datetime).format(formatter);
        } else if (datetime instanceof OffsetDateTime) {
            return ((OffsetDateTime) datetime).format(formatter);
        } else {
            return datetime.toString();
        }
    }
    
    /**
     * Calculate the age between two dates.
     * @param birthDate the birth date
     * @param currentDate the current date (can be null for current date)
     * @return the age in years
     */
    public static Period age(LocalDate birthDate, LocalDate currentDate) {
        if (birthDate == null) {
            return null;
        }
        if (currentDate == null) {
            currentDate = LocalDate.now(ZoneOffset.UTC);
        }
        return Period.between(birthDate, currentDate);
    }
}