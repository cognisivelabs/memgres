package com.memgres.functions;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import java.util.UUID;

/**
 * PostgreSQL-compatible UUID generation functions for MemGres.
 * Provides gen_random_uuid(), uuid_generate_v1(), and uuid_generate_v4() functions.
 */
public class UuidFunctions {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static volatile long clockSequence = -1;
    private static volatile byte[] nodeId = null;
    
    // Initialize node ID and clock sequence for UUID v1 generation
    static {
        initializeNodeIdAndClock();
    }
    
    /**
     * Generate a random UUID (equivalent to PostgreSQL's gen_random_uuid()).
     * Uses cryptographically secure random number generation.
     * 
     * @return a new random UUID
     */
    public static UUID genRandomUuid() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        
        // Set version (4) and variant bits according to RFC 4122
        randomBytes[6] = (byte) ((randomBytes[6] & 0x0f) | 0x40); // Version 4
        randomBytes[8] = (byte) ((randomBytes[8] & 0x3f) | 0x80); // Variant 10
        
        long mostSigBits = 0;
        long leastSigBits = 0;
        
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (randomBytes[i] & 0xff);
        }
        
        for (int i = 8; i < 16; i++) {
            leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xff);
        }
        
        return new UUID(mostSigBits, leastSigBits);
    }
    
    /**
     * Generate a UUID version 1 (time-based) with MAC address and timestamp.
     * Compatible with PostgreSQL's uuid_generate_v1().
     * 
     * @return a new time-based UUID
     */
    public static synchronized UUID uuidGenerateV1() {
        // Get current timestamp in 100-nanosecond intervals since UUID epoch (Oct 15, 1582)
        long currentTimeNanos = System.nanoTime();
        long currentTimeMillis = System.currentTimeMillis();
        long uuidTime = (currentTimeMillis + 12219292800000L) * 10000L; // Convert to UUID time
        
        // Add nanosecond precision to ensure uniqueness
        uuidTime += (currentTimeNanos % 10000L);
        
        // Time low (32 bits)
        long timeLow = uuidTime & 0xFFFFFFFFL;
        
        // Time mid (16 bits) 
        long timeMid = (uuidTime >> 32) & 0xFFFFL;
        
        // Time high and version (16 bits)
        long timeHiAndVersion = ((uuidTime >> 48) & 0x0FFFL) | 0x1000L; // Version 1
        
        // Generate unique clock sequence for each call to ensure uniqueness
        long uniqueClockSeq = (getClockSequence() + currentTimeNanos) & 0x3FFFL;
        long clockSeqAndNode = (uniqueClockSeq | 0x8000L); // Variant 10
        clockSeqAndNode = (clockSeqAndNode << 48) | (getNodeId() & 0xFFFFFFFFFFFFL);
        
        long mostSigBits = (timeLow << 32) | (timeMid << 16) | timeHiAndVersion;
        
        return new UUID(mostSigBits, clockSeqAndNode);
    }
    
    /**
     * Generate a UUID version 4 (random).
     * Compatible with PostgreSQL's uuid_generate_v4().
     * 
     * @return a new random UUID
     */
    public static UUID uuidGenerateV4() {
        return UUID.randomUUID();
    }
    
    /**
     * Initialize node ID and clock sequence for UUID v1 generation.
     */
    private static void initializeNodeIdAndClock() {
        try {
            // Try to get MAC address from network interface
            byte[] macAddress = getMacAddress();
            if (macAddress != null && macAddress.length >= 6) {
                nodeId = new byte[6];
                System.arraycopy(macAddress, 0, nodeId, 0, 6);
            } else {
                // Fallback to random node ID with multicast bit set
                nodeId = new byte[6];
                SECURE_RANDOM.nextBytes(nodeId);
                nodeId[0] |= 0x01; // Set multicast bit to indicate random node ID
            }
            
            // Initialize clock sequence with random value
            clockSequence = SECURE_RANDOM.nextInt(16384); // 14-bit value
            
        } catch (Exception e) {
            // Fallback to random values
            nodeId = new byte[6];
            SECURE_RANDOM.nextBytes(nodeId);
            nodeId[0] |= 0x01; // Set multicast bit
            clockSequence = SECURE_RANDOM.nextInt(16384);
        }
    }
    
    /**
     * Get MAC address from available network interfaces.
     */
    private static byte[] getMacAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] macAddress = networkInterface.getHardwareAddress();
                
                if (macAddress != null && macAddress.length == 6 && !isLocalAddress(macAddress)) {
                    return macAddress;
                }
            }
        } catch (Exception e) {
            // Ignore and return null to use random node ID
        }
        return null;
    }
    
    /**
     * Check if MAC address is a local/virtual address.
     */
    private static boolean isLocalAddress(byte[] macAddress) {
        // Check for common local/virtual MAC address patterns
        return macAddress[0] == 0x00 && macAddress[1] == 0x00 && macAddress[2] == 0x00;
    }
    
    /**
     * Get the clock sequence for UUID generation.
     */
    private static long getClockSequence() {
        return clockSequence;
    }
    
    /**
     * Get the node ID as a long value.
     */
    private static long getNodeId() {
        long result = 0;
        for (int i = 0; i < 6; i++) {
            result = (result << 8) | (nodeId[i] & 0xFF);
        }
        return result;
    }
}