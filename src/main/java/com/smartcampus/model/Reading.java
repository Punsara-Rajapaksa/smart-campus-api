package com.smartcampus.model;

import java.time.LocalDateTime;

public class Reading {
    private int id;
    private int sensorId;
    private double value;
    private String timestamp;

    public Reading() {}

    public Reading(int id, int sensorId, double value, String timestamp) {
        this.id = id;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSensorId() { return sensorId; }
    public void setSensorId(int sensorId) { this.sensorId = sensorId; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}