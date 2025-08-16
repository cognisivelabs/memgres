package com.memgres.triggers;

import com.memgres.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple test trigger for MemGres trigger testing.
 * This trigger logs all operations to a static list for verification in tests.
 */
public class TestTrigger implements Trigger {
    
    // Static list to track trigger fires for testing purposes
    public static final List<TriggerEvent> firedEvents = new ArrayList<>();
    
    public static class TriggerEvent {
        public final String triggerName;
        public final String tableName;
        public final boolean before;
        public final int type;
        public final Object[] oldRow;
        public final Object[] newRow;
        
        public TriggerEvent(String triggerName, String tableName, boolean before, int type, 
                          Object[] oldRow, Object[] newRow) {
            this.triggerName = triggerName;
            this.tableName = tableName;
            this.before = before;
            this.type = type;
            this.oldRow = oldRow != null ? Arrays.copyOf(oldRow, oldRow.length) : null;
            this.newRow = newRow != null ? Arrays.copyOf(newRow, newRow.length) : null;
        }
        
        @Override
        public String toString() {
            return String.format("TriggerEvent{name='%s', table='%s', before=%s, type=%s, oldRow=%s, newRow=%s}",
                    triggerName, tableName, before, getTypeName(type), 
                    oldRow != null ? Arrays.toString(oldRow) : "null",
                    newRow != null ? Arrays.toString(newRow) : "null");
        }
        
        private String getTypeName(int type) {
            switch (type) {
                case Trigger.INSERT: return "INSERT";
                case Trigger.UPDATE: return "UPDATE";
                case Trigger.DELETE: return "DELETE";
                case Trigger.SELECT: return "SELECT";
                case Trigger.ROLLBACK: return "ROLLBACK";
                default: return "UNKNOWN";
            }
        }
    }
    
    private String triggerName;
    private String tableName;
    private boolean before;
    private int type;
    
    @Override
    public void init(Connection conn, String schemaName, String triggerName, 
                    String tableName, boolean before, int type) throws SQLException {
        this.triggerName = triggerName;
        this.tableName = tableName;
        this.before = before;
        this.type = type;
    }
    
    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        // Record the trigger firing
        TriggerEvent event = new TriggerEvent(triggerName, tableName, before, type, oldRow, newRow);
        firedEvents.add(event);
        
        // Log the event (optional, useful for debugging)
        System.out.println("TestTrigger fired: " + event);
    }
    
    @Override
    public void close() throws SQLException {
        // Nothing to close for this test trigger
    }
    
    @Override
    public void remove() throws SQLException {
        // Nothing to remove for this test trigger
    }
    
    /**
     * Clear all recorded events (useful for test setup).
     */
    public static void clearEvents() {
        firedEvents.clear();
    }
    
    /**
     * Get all recorded events (useful for test verification).
     */
    public static List<TriggerEvent> getEvents() {
        return new ArrayList<>(firedEvents);
    }
    
    /**
     * Get events for a specific trigger name.
     */
    public static List<TriggerEvent> getEventsForTrigger(String triggerName) {
        return firedEvents.stream()
                .filter(event -> event.triggerName.equals(triggerName))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}