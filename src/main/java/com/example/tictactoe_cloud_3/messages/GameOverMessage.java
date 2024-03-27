package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;

import java.util.UUID;

public class GameOverMessage extends GameMessage {
    public GameOverMessage(UUID player, boolean isWinner){
        super(MessageType.END, player, "");
        setContent(isWinner?"Wygrałeś!":"Przegrałeś");
    }
}
