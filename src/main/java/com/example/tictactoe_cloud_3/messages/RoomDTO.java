package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;
import com.example.tictactoe_cloud_3.types.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class RoomDTO {

    private final MessageType type = MessageType.ROOM;
    private int roomNumber;
    private UUID player1;
    private UUID player2;
    private UUID playerStarting;
    private int freeSlots;


    public static RoomDTO of(Room room) {
        return RoomDTO.builder()
                .roomNumber(room.getRoomNumber())
                .freeSlots(room.getFreeSlots())
                .player1(room.getPlayer1() != null ? room.getPlayer1() : null)
                .player2(room.getPlayer2() != null ? room.getPlayer2() : null)
                .playerStarting(room.getPlayerStarting())
                .build();
    }

}