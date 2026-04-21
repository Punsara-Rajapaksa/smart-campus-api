package com.smartcampus.dao;

import com.smartcampus.model.Reading;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStore {
    // Thread-safe ID generators
    private static final AtomicInteger roomIdGenerator = new AtomicInteger(1);
    private static final AtomicInteger sensorIdGenerator = new AtomicInteger(1);

    // In-memory collections 
    public static final List<Room> rooms = new ArrayList<>();
    public static final List<Sensor> sensors = new ArrayList<>();
    
    // ID generator for readings (thread-safe)
    private static final AtomicInteger readingIdGenerator = new AtomicInteger(1);
    
    // In-memory list of all sensor readings
    public static final List<Reading> readings = new ArrayList<>();    

    // Static initializer with sample data
    static {
        // Add some initial rooms
        Room room1 = new Room(roomIdGenerator.getAndIncrement(), "Lecture Hall A", "Building 1", 120);
        Room room2 = new Room(roomIdGenerator.getAndIncrement(), "Lab 3", "Science Wing", 30);
        rooms.add(room1);
        rooms.add(room2);

        // Add some initial sensors
        Sensor sensor1 = new Sensor(sensorIdGenerator.getAndIncrement(), "CO2", "ACTIVE", 450.0, room1.getId());
        Sensor sensor2 = new Sensor(sensorIdGenerator.getAndIncrement(), "OCCUPANCY", "ACTIVE", 85.0, room1.getId());
        Sensor sensor3 = new Sensor(sensorIdGenerator.getAndIncrement(), "LIGHTING", "MAINTENANCE", 0.0, room2.getId());
        sensors.add(sensor1);
        sensors.add(sensor2);
        sensors.add(sensor3);
        
        // Sample readings for sensor 1 (CO2)
        readings.add(new Reading(readingIdGenerator.getAndIncrement(), 1, 450.0, LocalDateTime.now().minusHours(1).toString()));
        readings.add(new Reading(readingIdGenerator.getAndIncrement(), 1, 455.0, LocalDateTime.now().toString()));
        readings.add(new Reading(readingIdGenerator.getAndIncrement(), 2, 85.0, LocalDateTime.now().minusMinutes(30).toString()));        
    }

    // Utility methods for ID generation
    public static int nextRoomId() {
        return roomIdGenerator.getAndIncrement();
    }

    public static int nextSensorId() {
        return sensorIdGenerator.getAndIncrement();
    }
    
    public static int nextReadingId() {
        return readingIdGenerator.getAndIncrement();
    }    
}