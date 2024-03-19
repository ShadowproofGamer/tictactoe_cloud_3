package com.example.tictactoe_cloud_3.repo;

import com.example.tictactoe_cloud_3.types.Room;
import com.example.tictactoe_cloud_3.utils.exception.RoomNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class RoomRepo {
    private List<Room> rooms;

    private int roomCounter = 1;

    public RoomRepo() {
        rooms = new ArrayList<>(List.of(new Room(roomCounter++)));
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public Room getRoomByName(int roomNumber) {
        return rooms.stream()
                .filter(room -> room.getRoomNumber()==roomNumber)
                .findFirst()
                .orElseThrow(RoomNotFoundException::new);
    }

    public Optional<Room> getRoomWithOneFreeSlot() {
        return rooms.stream()
                .filter(room -> room.getFreeSlots() == 1)
                .findFirst();
    }

    public Optional<Room> getRoomWithTwoFreeSlots() {
        return rooms.stream()
                .filter(room -> room.getFreeSlots() == 2)
                .findFirst();
    }

    public int getRoomCounter() {
        return roomCounter;
    }

    public void setRoomCounter(int roomCounter) {
        this.roomCounter = roomCounter;
    }

}
