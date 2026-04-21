package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.model.Reading;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SensorReadingResource {

    private final int sensorId;

    // Constructor receives the sensorId from the parent resource
    public SensorReadingResource(int sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Reading> getReadings() {
        return DataStore.readings.stream()
                .filter(r -> r.getSensorId() == sensorId)
                .collect(Collectors.toList());
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(Reading reading) {
        // Find the parent sensor
        Sensor sensor = DataStore.sensors.stream()
                .filter(s -> s.getId() == sensorId)
                .findFirst()
                .orElse(null);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found")
                    .build();
        }

        // Ensure the sensor is not in MAINTENANCE 
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Sensor is in maintenance mode and cannot accept readings")
                    .build();
        }

        // Set IDs and timestamp
        reading.setId(DataStore.nextReadingId());
        reading.setSensorId(sensorId);
        reading.setTimestamp(LocalDateTime.now().toString());

        // Add to readings list
        DataStore.readings.add(reading);

        // Update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        // Build response
        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
    }
}