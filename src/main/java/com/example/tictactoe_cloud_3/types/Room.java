package com.example.tictactoe_cloud_3.types;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private int roomNumber;
    private String player1;
    private String player2;
    private String playerStarting;
    private final int maxSlots=2;
    private int freeSlots;

    public Room(int roomNumber){
        this.roomNumber=roomNumber;
        this.freeSlots=maxSlots;
    }



}
