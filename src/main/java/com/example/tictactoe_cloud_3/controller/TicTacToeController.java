package com.example.tictactoe_cloud_3.controller;

import com.example.tictactoe_cloud_3.messages.GameMessage;
import com.example.tictactoe_cloud_3.messages.RoomDTO;
import com.example.tictactoe_cloud_3.service.TicTacToeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Controller
@Slf4j
public class TicTacToeController {

    private static final Logger logger = LoggerFactory.getLogger(TicTacToeController.class);
    private final TicTacToeService ticTacToeService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TicTacToeController(TicTacToeService ticTacToeService, SimpMessagingTemplate messagingTemplate) {
        this.ticTacToeService = ticTacToeService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room/{roomID}")
    public void handleMove(
            @Payload GameMessage gameMessage,
            @PathVariable("roomID") int roomNumber
    ) {
        log.info("handling move from user {} in room {}", gameMessage.getUsername(), roomNumber);
        ticTacToeService.handleMove(roomNumber, gameMessage);
    }
//    @MessageMapping("/topic/{username}")
//    @SendTo("/topic/{username}")
//    public GameMessage handleMove(
//            @Payload GameMessage gameMessage,
//            @PathVariable("username") String username
//    ) {
//        return ticTacToeService.handleMessage(gameMessage);
//    }

    @MessageMapping("/topic/lobby")
    public void joinGame(
            @Payload GameMessage gameMessage,
            SimpMessageHeaderAccessor headerAccessor,
            @PathVariable("username") String username
    ){
        username = gameMessage.getUsername();
        log.info("entering function joinGame: "+username);
        headerAccessor.getSessionAttributes().put("username", username);
        RoomDTO roomDTO = ticTacToeService.chooseRoomForPlayer(username);
        headerAccessor.getSessionAttributes().put("room", roomDTO.getRoomNumber());
        messagingTemplate.convertAndSend("/topic/"+username, roomDTO);
        log.info("exiting function joinGame: "+username);
    }
}