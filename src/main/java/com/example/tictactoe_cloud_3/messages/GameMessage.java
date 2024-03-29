package com.example.tictactoe_cloud_3.messages;

import com.example.tictactoe_cloud_3.types.MessageType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMessage {

    protected MessageType type;
    protected UUID username;
    protected String content;
}