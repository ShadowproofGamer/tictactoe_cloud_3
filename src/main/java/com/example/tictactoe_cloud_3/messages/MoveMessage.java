package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;

public class MoveMessage extends GameMessage{
    public MoveMessage(String player, int move){
        super(MessageType.MOVE, player, String.valueOf(move));
    }
}
