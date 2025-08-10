package com.memgres.functions;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.temporal.TemporalAccessor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for date/time functions in MemGres.
 */
class DateTimeFunctionsTest {
    
    @Test
    void testNow() {
        ZonedDateTime before = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime result = DateTimeFunctions.now();
        ZonedDateTime after = ZonedDateTime.now(ZoneOffset.UTC);
        
        assertNotNull(result);
        assertTrue(result.isAfter(before.minusSeconds(1)) || result.isEqual(before));
        assertTrue(result.isBefore(after.plusSeconds(1)) || result.isEqual(after));
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }
    
    @Test
    void testCurrentDate() {
        LocalDate before = LocalDate.now(ZoneOffset.UTC);
        LocalDate result = DateTimeFunctions.currentDate();
        LocalDate after = LocalDate.now(ZoneOffset.UTC);
        
        assertNotNull(result);
        assertTrue(result.isEqual(before) || result.isEqual(after));
    }
    
    @Test
    void testCurrentTime() {
        OffsetTime result = DateTimeFunctions.currentTime();
        
        assertNotNull(result);
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }
    
    @Test
    void testCurrentTimestamp() {
        ZonedDateTime before = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime result = DateTimeFunctions.currentTimestamp();
        ZonedDateTime after = ZonedDateTime.now(ZoneOffset.UTC);
        
        assertNotNull(result);
        assertTrue(result.isAfter(before.minusSeconds(1)) || result.isEqual(before));
        assertTrue(result.isBefore(after.plusSeconds(1)) || result.isEqual(after));
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }
    
    @Test
    void testExtractFromLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 500_000_000);
        
        assertEquals(2023.0, DateTimeFunctions.extract("year", datetime));
        assertEquals(12.0, DateTimeFunctions.extract("month", datetime));
        assertEquals(25.0, DateTimeFunctions.extract("day", datetime));
        assertEquals(14.0, DateTimeFunctions.extract("hour", datetime));
        assertEquals(30.0, DateTimeFunctions.extract("minute", datetime));
        assertEquals(45.5, DateTimeFunctions.extract("second", datetime));
        assertEquals(500.0, DateTimeFunctions.extract("millisecond", datetime));
        assertEquals(500_000.0, DateTimeFunctions.extract("microsecond", datetime));
        assertEquals(500_000_000.0, DateTimeFunctions.extract("nanosecond", datetime));
        assertEquals(4.0, DateTimeFunctions.extract("quarter", datetime));
        assertEquals(359.0, DateTimeFunctions.extract("doy", datetime));
    }
    
    @Test
    void testExtractFromLocalDate() {
        LocalDate date = LocalDate.of(2023, 6, 15);
        
        assertEquals(2023.0, DateTimeFunctions.extract("year", date));
        assertEquals(6.0, DateTimeFunctions.extract("month", date));
        assertEquals(15.0, DateTimeFunctions.extract("day", date));
        assertEquals(2.0, DateTimeFunctions.extract("quarter", date));
        assertEquals(166.0, DateTimeFunctions.extract("doy", date));
    }
    
    @Test
    void testExtractDayOfWeek() {
        // Test day of week extraction (PostgreSQL format: Sunday=0, Monday=1, etc.)
        LocalDate monday = LocalDate.of(2023, 12, 25); // 2023-12-25 is a Monday
        LocalDate tuesday = LocalDate.of(2023, 12, 26);
        LocalDate sunday = LocalDate.of(2023, 12, 24); // 2023-12-24 is a Sunday
        
        assertEquals(1.0, DateTimeFunctions.extract("dow", monday)); // Monday = 1
        assertEquals(2.0, DateTimeFunctions.extract("dow", tuesday)); // Tuesday = 2
        assertEquals(0.0, DateTimeFunctions.extract("dow", sunday));  // Sunday = 0
    }
    
    @Test
    void testExtractEpoch() {
        // Test epoch extraction
        ZonedDateTime dateTime = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        Double epoch = DateTimeFunctions.extract("epoch", dateTime);
        
        assertNotNull(epoch);
        assertEquals(1672531200.0, epoch); // 2023-01-01 00:00:00 UTC in epoch seconds
    }
    
    @Test
    void testExtractNullValues() {
        assertNull(DateTimeFunctions.extract("year", null));
        assertNull(DateTimeFunctions.extract(null, LocalDateTime.now()));
        assertNull(DateTimeFunctions.extract(null, null));
    }
    
    @Test
    void testExtractInvalidField() {
        LocalDateTime datetime = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, 
            () -> DateTimeFunctions.extract("invalid_field", datetime));
    }
    
    @Test
    void testDateAddToLocalDateTime() {
        LocalDateTime base = LocalDateTime.of(2023, 1, 15, 10, 30, 0);
        
        // Test adding years
        TemporalAccessor result = DateTimeFunctions.dateAdd(base, "1 year");
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 0), result);
        
        // Test adding months
        result = DateTimeFunctions.dateAdd(base, "2 months");
        assertEquals(LocalDateTime.of(2023, 3, 15, 10, 30, 0), result);
        
        // Test adding days
        result = DateTimeFunctions.dateAdd(base, "10 days");
        assertEquals(LocalDateTime.of(2023, 1, 25, 10, 30, 0), result);
        
        // Test adding hours
        result = DateTimeFunctions.dateAdd(base, "3 hours");
        assertEquals(LocalDateTime.of(2023, 1, 15, 13, 30, 0), result);
        
        // Test adding minutes
        result = DateTimeFunctions.dateAdd(base, "45 minutes");
        assertEquals(LocalDateTime.of(2023, 1, 15, 11, 15, 0), result);
        
        // Test adding seconds
        result = DateTimeFunctions.dateAdd(base, "30 seconds");
        assertEquals(LocalDateTime.of(2023, 1, 15, 10, 30, 30), result);
    }
    
    @Test
    void testDateAddToLocalDate() {
        LocalDate base = LocalDate.of(2023, 6, 15);
        
        // Test adding years
        TemporalAccessor result = DateTimeFunctions.dateAdd(base, "1 year");
        assertEquals(LocalDate.of(2024, 6, 15), result);
        
        // Test adding months
        result = DateTimeFunctions.dateAdd(base, "3 months");
        assertEquals(LocalDate.of(2023, 9, 15), result);
        
        // Test adding days
        result = DateTimeFunctions.dateAdd(base, "7 days");
        assertEquals(LocalDate.of(2023, 6, 22), result);
        
        // Test that time units cannot be added to date
        assertThrows(IllegalArgumentException.class, 
            () -> DateTimeFunctions.dateAdd(base, "2 hours"));
    }
    
    @Test
    void testDateAddToZonedDateTime() {
        ZonedDateTime base = ZonedDateTime.of(2023, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        
        TemporalAccessor result = DateTimeFunctions.dateAdd(base, "1 day");
        assertEquals(ZonedDateTime.of(2023, 1, 16, 10, 30, 0, 0, ZoneOffset.UTC), result);
    }
    
    @Test
    void testDateAddNullValues() {
        assertNull(DateTimeFunctions.dateAdd(null, "1 day"));
        assertNull(DateTimeFunctions.dateAdd(LocalDateTime.now(), null));
    }
    
    @Test
    void testDateAddInvalidInterval() {
        LocalDateTime base = LocalDateTime.now();
        
        // Invalid format
        assertThrows(IllegalArgumentException.class, 
            () -> DateTimeFunctions.dateAdd(base, "invalid"));
        
        // Invalid amount
        assertThrows(IllegalArgumentException.class, 
            () -> DateTimeFunctions.dateAdd(base, "abc days"));
        
        // Invalid unit
        assertThrows(IllegalArgumentException.class, 
            () -> DateTimeFunctions.dateAdd(base, "1 invalid_unit"));
    }
    
    @Test
    void testFormatDateTime() {
        LocalDateTime datetime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        
        // Test basic formatting
        String result = DateTimeFunctions.formatDateTime(datetime, "yyyy-MM-dd HH:mm:ss");
        assertEquals("2023-12-25 14:30:45", result);
        
        // Test PostgreSQL-style pattern conversion
        result = DateTimeFunctions.formatDateTime(datetime, "YYYY-MM-DD HH24:MI:SS");
        assertEquals("2023-12-25 14:30:45", result);
        
        // Test LocalDate formatting
        LocalDate date = LocalDate.of(2023, 12, 25);
        result = DateTimeFunctions.formatDateTime(date, "yyyy-MM-dd");
        assertEquals("2023-12-25", result);
        
        // Test LocalTime formatting
        LocalTime time = LocalTime.of(14, 30, 45);
        result = DateTimeFunctions.formatDateTime(time, "HH:mm:ss");
        assertEquals("14:30:45", result);
    }
    
    @Test
    void testFormatDateTimeNullValues() {
        assertNull(DateTimeFunctions.formatDateTime(null, "yyyy-MM-dd"));
        assertNull(DateTimeFunctions.formatDateTime(LocalDateTime.now(), null));
    }
    
    @Test
    void testAge() {
        LocalDate birthDate = LocalDate.of(1990, 6, 15);
        LocalDate currentDate = LocalDate.of(2023, 8, 20);
        
        Period age = DateTimeFunctions.age(birthDate, currentDate);
        assertNotNull(age);
        assertEquals(33, age.getYears());
        assertEquals(2, age.getMonths());
        assertEquals(5, age.getDays());
        
        // Test with null current date (should use current date)
        Period ageWithCurrentDate = DateTimeFunctions.age(birthDate, null);
        assertNotNull(ageWithCurrentDate);
        assertTrue(ageWithCurrentDate.getYears() >= 33); // Should be at least 33
    }
    
    @Test
    void testAgeNullBirthDate() {
        assertNull(DateTimeFunctions.age(null, LocalDate.now()));
    }
    
    @Test
    void testCaseInsensitiveExtractFields() {
        LocalDateTime datetime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        
        // Test case insensitivity
        assertEquals(2023.0, DateTimeFunctions.extract("YEAR", datetime));
        assertEquals(2023.0, DateTimeFunctions.extract("Year", datetime));
        assertEquals(2023.0, DateTimeFunctions.extract("year", datetime));
        
        assertEquals(12.0, DateTimeFunctions.extract("MONTH", datetime));
        assertEquals(25.0, DateTimeFunctions.extract("DAY", datetime));
    }
    
    @Test
    void testPluralAndSingularUnits() {
        LocalDateTime base = LocalDateTime.of(2023, 1, 15, 10, 30, 0);
        
        // Test that both singular and plural forms work
        TemporalAccessor result1 = DateTimeFunctions.dateAdd(base, "1 year");
        TemporalAccessor result2 = DateTimeFunctions.dateAdd(base, "1 years");
        assertEquals(result1, result2);
        
        result1 = DateTimeFunctions.dateAdd(base, "1 day");
        result2 = DateTimeFunctions.dateAdd(base, "1 days");
        assertEquals(result1, result2);
        
        result1 = DateTimeFunctions.dateAdd(base, "1 hour");
        result2 = DateTimeFunctions.dateAdd(base, "1 hours");
        assertEquals(result1, result2);
    }
}