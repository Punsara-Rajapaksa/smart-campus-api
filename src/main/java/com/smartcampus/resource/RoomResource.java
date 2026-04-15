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

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms - List all rooms
    @GET
    public List<Room> getAllRooms() {
        return DataStore.rooms;
    }

    // GET /api/v1/rooms/{id} - Get a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") int roomId) {
        Room room = DataStore.rooms.stream()
                .filter(r -> r.getId() == roomId)
                .findFirst()
                .orElse(null);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(room).build();
    }

    // POST /api/v1/rooms - Create a new room
    @POST
    public Response createRoom(Room room) {
        // Assign a new ID
        room.setId(DataStore.nextRoomId());
        DataStore.rooms.add(room);
        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // DELETE /api/v1/rooms/{id} - Delete a room only if no sensors are assigned
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") int roomId) {
        Room room = DataStore.rooms.stream()
                .filter(r -> r.getId() == roomId)
                .findFirst()
                .orElse(null);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if any sensor references this room
        boolean hasSensors = DataStore.sensors.stream()
                .anyMatch(s -> s.getRoomId() == roomId);
        if (hasSensors) {
            // Return 409 Conflict 
            return Response.status(Response.Status.CONFLICT)
                    .entity("Cannot delete room with active sensors")
                    .build();
        }

        DataStore.rooms.remove(room);
        return Response.noContent().build(); // 204 No Content
    }
}