package com.memgres.types;

import java.time.Duration;
import java.time.Period;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an H2-compatible INTERVAL data type.
 * Supports both year-month intervals and day-time intervals.
 */
public class Interval {
    
    public enum IntervalType {
        YEAR_MONTH,  // Years and months
        DAY_TIME     // Days, hours, minutes, seconds
    }
    
    private final IntervalType type;
    private final Period period;      // For year-month intervals
    private final Duration duration;  // For day-time intervals
    
    // Constructors
    private Interval(IntervalType type, Period period, Duration duration) {
        this.type = type;
        this.period = period;
        this.duration = duration;
    }
    
    /**
     * Create a year-month interval
     */
    public static Interval ofYearMonth(int years, int months) {
        return new Interval(IntervalType.YEAR_MONTH, Period.of(years, months, 0), null);
    }
    
    /**
     * Create a day-time interval
     */
    public static Interval ofDayTime(int days, int hours, int minutes, int seconds) {
        return new Interval(IntervalType.DAY_TIME, null, 
            Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds));
    }
    
    /**
     * Parse an interval string (H2 format)
     */
    public static Interval parse(String intervalString) {
        if (intervalString == null || intervalString.trim().isEmpty()) {
            throw new IllegalArgumentException("Interval string cannot be null or empty");
        }
        
        String str = intervalString.trim().toUpperCase();
        
        // Pattern for various interval formats
        // Examples: "1 YEAR", "2 MONTHS", "5 DAYS", "3 HOURS", "1-2" (year-month), "1 2:30:45" (day-time)
        
        // Simple single unit patterns
        Pattern yearPattern = Pattern.compile("^(\\d+)\\s+YEARS?$");
        Pattern monthPattern = Pattern.compile("^(\\d+)\\s+MONTHS?$");
        Pattern weekPattern = Pattern.compile("^(\\d+)\\s+WEEKS?$");
        Pattern dayPattern = Pattern.compile("^(\\d+)\\s+DAYS?$");
        Pattern hourPattern = Pattern.compile("^(\\d+)\\s+HOURS?$");
        Pattern minutePattern = Pattern.compile("^(\\d+)\\s+MINUTES?$");
        Pattern secondPattern = Pattern.compile("^(\\d+)\\s+SECONDS?$");
        
        // Year-month format: "1-2" (1 year, 2 months)
        Pattern yearMonthPattern = Pattern.compile("^(\\d+)-(\\d+)$");
        
        // Day-time format: "1 2:30:45" (1 day, 2 hours, 30 minutes, 45 seconds)
        Pattern dayTimePattern = Pattern.compile("^(\\d+)\\s+(\\d+):(\\d+):(\\d+)$");
        
        Matcher matcher;
        
        if ((matcher = yearPattern.matcher(str)).matches()) {
            return ofYearMonth(Integer.parseInt(matcher.group(1)), 0);
        } else if ((matcher = monthPattern.matcher(str)).matches()) {
            return ofYearMonth(0, Integer.parseInt(matcher.group(1)));
        } else if ((matcher = weekPattern.matcher(str)).matches()) {
            return ofDayTime(Integer.parseInt(matcher.group(1)) * 7, 0, 0, 0);
        } else if ((matcher = dayPattern.matcher(str)).matches()) {
            return ofDayTime(Integer.parseInt(matcher.group(1)), 0, 0, 0);
        } else if ((matcher = hourPattern.matcher(str)).matches()) {
            return ofDayTime(0, Integer.parseInt(matcher.group(1)), 0, 0);
        } else if ((matcher = minutePattern.matcher(str)).matches()) {
            return ofDayTime(0, 0, Integer.parseInt(matcher.group(1)), 0);
        } else if ((matcher = secondPattern.matcher(str)).matches()) {
            return ofDayTime(0, 0, 0, Integer.parseInt(matcher.group(1)));
        } else if ((matcher = yearMonthPattern.matcher(str)).matches()) {
            return ofYearMonth(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        } else if ((matcher = dayTimePattern.matcher(str)).matches()) {
            return ofDayTime(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4))
            );
        } else {
            throw new IllegalArgumentException("Invalid interval format: " + intervalString);
        }
    }
    
    // Getters
    public IntervalType getType() {
        return type;
    }
    
    public Period getPeriod() {
        return period;
    }
    
    public Duration getDuration() {
        return duration;
    }
    
    // Helper methods
    public int getYears() {
        return type == IntervalType.YEAR_MONTH ? period.getYears() : 0;
    }
    
    public int getMonths() {
        return type == IntervalType.YEAR_MONTH ? period.getMonths() : 0;
    }
    
    public int getDays() {
        return type == IntervalType.DAY_TIME ? (int) duration.toDays() : 0;
    }
    
    public int getHours() {
        if (type == IntervalType.DAY_TIME) {
            long totalHours = duration.toHours();
            return (int) (totalHours % 24);
        }
        return 0;
    }
    
    public int getMinutes() {
        if (type == IntervalType.DAY_TIME) {
            long totalMinutes = duration.toMinutes();
            return (int) (totalMinutes % 60);
        }
        return 0;
    }
    
    public int getSeconds() {
        if (type == IntervalType.DAY_TIME) {
            long totalSeconds = duration.getSeconds();
            return (int) (totalSeconds % 60);
        }
        return 0;
    }
    
    @Override
    public String toString() {
        if (type == IntervalType.YEAR_MONTH) {
            if (period.getYears() == 0 && period.getMonths() == 0) {
                return "0 MONTHS";
            } else if (period.getYears() == 0) {
                return period.getMonths() + (period.getMonths() == 1 ? " MONTH" : " MONTHS");
            } else if (period.getMonths() == 0) {
                return period.getYears() + (period.getYears() == 1 ? " YEAR" : " YEARS");
            } else {
                return period.getYears() + "-" + period.getMonths();
            }
        } else {
            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            
            if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
                return "0 SECONDS";
            } else if (days == 0 && hours == 0 && minutes == 0) {
                return seconds + (seconds == 1 ? " SECOND" : " SECONDS");
            } else if (days == 0 && hours == 0) {
                return minutes + (minutes == 1 ? " MINUTE" : " MINUTES");
            } else if (days == 0) {
                return hours + (hours == 1 ? " HOUR" : " HOURS");
            } else if (hours == 0 && minutes == 0 && seconds == 0) {
                return days + (days == 1 ? " DAY" : " DAYS");
            } else {
                return String.format("%d %02d:%02d:%02d", days, hours, minutes, seconds);
            }
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Interval interval = (Interval) obj;
        return type == interval.type &&
               Objects.equals(period, interval.period) &&
               Objects.equals(duration, interval.duration);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, period, duration);
    }
}