package com.example.tictactoe_cloud_3.config;

import com.example.tictactoe_cloud_3.messages.GameMessage;
import com.example.tictactoe_cloud_3.service.TicTacToeService;
import com.example.tictactoe_cloud_3.types.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messageTemplate;
    private final TicTacToeService ticTacToeService;

    @EventListener
    public void handleWebSockerDisconnectionListener(
            SessionDisconnectEvent event
    ) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        try{
            UUID username = (UUID) headerAccessor.getSessionAttributes().get("guid");
            try{
                int roomNumber = (int) headerAccessor.getSessionAttributes().get("room");
                if (username != null) {
                    log.info("User disconnected: {}", username);
//                    ticTacToeService.deletePlayerFromRoom(roomNumber, username);
                    ticTacToeService.handleMove(roomNumber, new GameMessage(MessageType.LEAVE, username, "User left"));
                }
            }catch (NullPointerException e){
                log.warn("User disconnected with error {}: {}", e.toString(), username);
            }
        }catch (NullPointerException error){
            log.error("Unhandled user disconnect! Error:{}", error.toString());
        }


    }
}
