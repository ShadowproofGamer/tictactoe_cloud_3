package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;

public class GameStartMessage extends GameMessage{
    public GameStartMessage(String player){
        super(MessageType.START, player, "Game started!");
    }
}
