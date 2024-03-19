package com.example.tictactoe_cloud_3.types;

public enum MessageType {
    //user message types:
    LOGIN,
    START,
    END,
    MOVE,

    //game message types:
    LEAVE,
    ROOM,

    RESPONSEOK,
    RESPONSEERROR
}
