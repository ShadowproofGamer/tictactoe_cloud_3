package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;

public class OpponentLeftMessage extends GameMessage{
    public OpponentLeftMessage(String opponent){
        super(MessageType.LEAVE, opponent, "Opponent left!");
    }
}
