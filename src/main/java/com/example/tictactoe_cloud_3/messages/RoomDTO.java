package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;
import com.example.tictactoe_cloud_3.types.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoomDTO {

    private final MessageType type = MessageType.ROOM;
    private int roomNumber;
    private String player1;
    private String player2;
    private String playerStarting;
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