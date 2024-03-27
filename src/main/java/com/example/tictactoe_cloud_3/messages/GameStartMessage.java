package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;

import java.util.UUID;

public class GameStartMessage extends GameMessage{
    public GameStartMessage(UUID player){
        super(MessageType.START, player, "Game started!");
    }
}
