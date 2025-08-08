package com.memgres.functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Comprehensive tests for UUID generation functions.
 * Tests PostgreSQL compatibility and UUID specification compliance.
 */
class UuidFunctionsTest {
    
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    @Test
    void testGenRandomUuidFormat() {
        UUID uuid = UuidFunctions.genRandomUuid();
        
        assertNotNull(uuid, "Generated UUID should not be null");
        assertTrue(UUID_PATTERN.matcher(uuid.toString()).matches(), 
                   "UUID should match standard format");
        assertEquals(4, uuid.version(), "gen_random_uuid should generate version 4 UUIDs");
        assertEquals(2, uuid.variant(), "UUID should have RFC 4122 variant");
    }
    
    @RepeatedTest(100)
    void testGenRandomUuidUniqueness() {
        Set<UUID> uuids = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            UUID uuid = UuidFunctions.genRandomUuid();
            assertFalse(uuids.contains(uuid), 
                       "Generated UUID should be unique: " + uuid);
            uuids.add(uuid);
        }
    }
    
    @Test
    void testUuidGenerateV1Format() {
        UUID uuid = UuidFunctions.uuidGenerateV1();
        
        assertNotNull(uuid, "Generated UUID should not be null");
        assertTrue(UUID_PATTERN.matcher(uuid.toString()).matches(), 
                   "UUID should match standard format");
        assertEquals(1, uuid.version(), "uuid_generate_v1 should generate version 1 UUIDs");
        assertEquals(2, uuid.variant(), "UUID should have RFC 4122 variant");
    }
    
    @Test
    void testUuidGenerateV1Ordering() {
        UUID uuid1 = UuidFunctions.uuidGenerateV1();
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        UUID uuid2 = UuidFunctions.uuidGenerateV1();
        
        // Version 1 UUIDs should be time-ordered (most significant bits contain timestamp)
        assertTrue(uuid1.getMostSignificantBits() <= uuid2.getMostSignificantBits(),
                   "V1 UUIDs should be time-ordered");
    }
    
    @RepeatedTest(50)
    void testUuidGenerateV1Uniqueness() {
        Set<UUID> uuids = new HashSet<>();
        
        for (int i = 0; i < 100; i++) {
            UUID uuid = UuidFunctions.uuidGenerateV1();
            assertFalse(uuids.contains(uuid), 
                       "Generated V1 UUID should be unique: " + uuid);
            uuids.add(uuid);
        }
    }
    
    @Test
    void testUuidGenerateV4Format() {
        UUID uuid = UuidFunctions.uuidGenerateV4();
        
        assertNotNull(uuid, "Generated UUID should not be null");
        assertTrue(UUID_PATTERN.matcher(uuid.toString()).matches(), 
                   "UUID should match standard format");
        assertEquals(4, uuid.version(), "uuid_generate_v4 should generate version 4 UUIDs");
        assertEquals(2, uuid.variant(), "UUID should have RFC 4122 variant");
    }
    
    @RepeatedTest(100)
    void testUuidGenerateV4Uniqueness() {
        Set<UUID> uuids = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            UUID uuid = UuidFunctions.uuidGenerateV4();
            assertFalse(uuids.contains(uuid), 
                       "Generated V4 UUID should be unique: " + uuid);
            uuids.add(uuid);
        }
    }
    
    @Test
    void testGenRandomUuidVsUuidGenerateV4Compatibility() {
        UUID randomUuid = UuidFunctions.genRandomUuid();
        UUID v4Uuid = UuidFunctions.uuidGenerateV4();
        
        // Both should be version 4 UUIDs
        assertEquals(4, randomUuid.version(), "gen_random_uuid should produce version 4");
        assertEquals(4, v4Uuid.version(), "uuid_generate_v4 should produce version 4");
        
        // Both should have proper variant
        assertEquals(2, randomUuid.variant(), "gen_random_uuid should have RFC 4122 variant");
        assertEquals(2, v4Uuid.variant(), "uuid_generate_v4 should have RFC 4122 variant");
    }
    
    @Test
    void testUuidVersionBitPatterns() {
        // Test version 1 UUID bit patterns
        UUID v1Uuid = UuidFunctions.uuidGenerateV1();
        long mostSigBits = v1Uuid.getMostSignificantBits();
        long versionBits = (mostSigBits >> 12) & 0xF;
        assertEquals(1, versionBits, "Version 1 UUID should have correct version bits");
        
        long leastSigBits = v1Uuid.getLeastSignificantBits();
        long variantBits = (leastSigBits >> 62) & 0x3;
        assertEquals(2, variantBits, "Version 1 UUID should have correct variant bits");
        
        // Test version 4 UUID bit patterns
        UUID v4Uuid = UuidFunctions.genRandomUuid();
        mostSigBits = v4Uuid.getMostSignificantBits();
        versionBits = (mostSigBits >> 12) & 0xF;
        assertEquals(4, versionBits, "Version 4 UUID should have correct version bits");
        
        leastSigBits = v4Uuid.getLeastSignificantBits();
        variantBits = (leastSigBits >> 62) & 0x3;
        assertEquals(2, variantBits, "Version 4 UUID should have correct variant bits");
    }
    
    @Test
    void testUuidStringRepresentation() {
        UUID uuid = UuidFunctions.genRandomUuid();
        String uuidString = uuid.toString();
        
        // Test format: 8-4-4-4-12 hexadecimal characters
        assertEquals(36, uuidString.length(), "UUID string should be 36 characters long");
        assertEquals(4, uuidString.chars().mapToObj(c -> (char) c).mapToInt(c -> c == '-' ? 1 : 0).sum(),
                    "UUID string should have 4 hyphens");
        
        // Test that it can be parsed back
        UUID parsedUuid = UUID.fromString(uuidString);
        assertEquals(uuid, parsedUuid, "UUID should be parseable from its string representation");
    }
    
    @Test
    void testPostgreSQLCompatibility() {
        // Test that our UUID functions produce valid PostgreSQL-compatible UUIDs
        UUID genRandomUuid = UuidFunctions.genRandomUuid();
        UUID uuidV1 = UuidFunctions.uuidGenerateV1();
        UUID uuidV4 = UuidFunctions.uuidGenerateV4();
        
        // All should be valid Java UUIDs (which are PostgreSQL compatible)
        assertDoesNotThrow(() -> UUID.fromString(genRandomUuid.toString()));
        assertDoesNotThrow(() -> UUID.fromString(uuidV1.toString()));
        assertDoesNotThrow(() -> UUID.fromString(uuidV4.toString()));
        
        // Test with DataType.UUID conversion
        assertTrue(com.memgres.types.DataType.UUID.isValidValue(genRandomUuid));
        assertTrue(com.memgres.types.DataType.UUID.isValidValue(uuidV1));
        assertTrue(com.memgres.types.DataType.UUID.isValidValue(uuidV4));
        
        assertEquals(genRandomUuid, com.memgres.types.DataType.UUID.convertValue(genRandomUuid.toString()));
        assertEquals(uuidV1, com.memgres.types.DataType.UUID.convertValue(uuidV1.toString()));
        assertEquals(uuidV4, com.memgres.types.DataType.UUID.convertValue(uuidV4.toString()));
    }
    
    @Test
    void testPerformance() {
        // Test that UUID generation is reasonably fast
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 10000; i++) {
            UuidFunctions.genRandomUuid();
        }
        
        long duration = System.nanoTime() - startTime;
        double averageTimePerUuid = duration / 10000.0;
        
        // Should generate UUIDs in reasonable time (less than 1ms per UUID on average)
        assertTrue(averageTimePerUuid < 1_000_000, 
                   "UUID generation should be fast (avg: " + averageTimePerUuid + " ns)");
    }
    
    @Test
    void testDataTypeIntegration() {
        // Test UUID generation via DataType.UuidGenerator utility
        UUID genRandomUuid = com.memgres.types.DataType.UuidGenerator.genRandomUuid();
        UUID uuidV1 = com.memgres.types.DataType.UuidGenerator.uuidGenerateV1();
        UUID uuidV4 = com.memgres.types.DataType.UuidGenerator.uuidGenerateV4();
        
        assertNotNull(genRandomUuid, "DataType.UuidGenerator.genRandomUuid() should return a UUID");
        assertNotNull(uuidV1, "DataType.UuidGenerator.uuidGenerateV1() should return a UUID");
        assertNotNull(uuidV4, "DataType.UuidGenerator.uuidGenerateV4() should return a UUID");
        
        assertEquals(4, genRandomUuid.version(), "genRandomUuid should return version 4 UUID");
        assertEquals(1, uuidV1.version(), "uuidGenerateV1 should return version 1 UUID");
        assertEquals(4, uuidV4.version(), "uuidGenerateV4 should return version 4 UUID");
        
        // Test that generated UUIDs are valid for the DataType
        com.memgres.types.DataType uuidDataType = com.memgres.types.DataType.UUID;
        assertTrue(uuidDataType.isValidValue(genRandomUuid));
        assertTrue(uuidDataType.isValidValue(uuidV1));
        assertTrue(uuidDataType.isValidValue(uuidV4));
        
        // Test that both static utility and instance methods work consistently
        UUID genRandomUuid2 = com.memgres.types.DataType.UuidGenerator.genRandomUuid();
        UUID uuidV12 = com.memgres.types.DataType.UuidGenerator.uuidGenerateV1();
        UUID uuidV42 = com.memgres.types.DataType.UuidGenerator.uuidGenerateV4();
        
        assertNotEquals(genRandomUuid, genRandomUuid2, "Multiple calls should generate different UUIDs");
        assertNotEquals(uuidV1, uuidV12, "Multiple calls should generate different UUIDs");
        assertNotEquals(uuidV4, uuidV42, "Multiple calls should generate different UUIDs");
    }
}