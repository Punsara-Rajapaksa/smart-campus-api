package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors?type=CO2 (optional filtering)
    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null || type.isEmpty()) {
            return DataStore.sensors;
        }
        // Filter by type (case‑insensitive)
        return DataStore.sensors.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // POST /api/v1/sensors – create new sensor (validates roomId exists)
    @POST
    public Response createSensor(Sensor sensor) {
        // Validate room exists
        boolean roomExists = DataStore.rooms.stream()
                .anyMatch(r -> r.getId() == sensor.getRoomId());
        if (!roomExists) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room with ID " + sensor.getRoomId() + " does not exist")
                    .build();
        }

        // Assign new ID and add to store
        sensor.setId(DataStore.nextSensorId());
        DataStore.sensors.add(sensor);

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }
}