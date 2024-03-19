package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;

public class GameOverMessage extends GameMessage {
    public GameOverMessage(String player, boolean isWinner){
        super(MessageType.END, player, "");
        setContent(isWinner?"Wygrałeś!":"Przegrałeś");
    }
}
