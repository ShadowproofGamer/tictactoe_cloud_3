package com.example.tictactoe_cloud_3.types;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private int roomNumber;
    private UUID player1;
    private UUID player2;
    private UUID playerStarting;
    private final int maxSlots=2;
    private int freeSlots;

    public Room(int roomNumber){
        this.roomNumber=roomNumber;
        this.freeSlots=maxSlots;
    }



}
