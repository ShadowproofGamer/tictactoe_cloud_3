package com.example.tictactoe_cloud_3.controller;

import com.example.tictactoe_cloud_3.messages.GameMessage;
import com.example.tictactoe_cloud_3.messages.JoinMessage;
import com.example.tictactoe_cloud_3.messages.RoomDTO;
import com.example.tictactoe_cloud_3.repo.PlayerRepo;
import com.example.tictactoe_cloud_3.service.TicTacToeService;
import com.example.tictactoe_cloud_3.types.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.UUID;

@Controller
@Slf4j
public class TicTacToeController {

    private static final Logger logger = LoggerFactory.getLogger(TicTacToeController.class);
    private final TicTacToeService ticTacToeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerRepo playerRepo;

    @Autowired
    public TicTacToeController(TicTacToeService ticTacToeService, SimpMessagingTemplate messagingTemplate, PlayerRepo playerRepo) {
        this.ticTacToeService = ticTacToeService;
        this.messagingTemplate = messagingTemplate;
        this.playerRepo = playerRepo;
    }

    @MessageMapping("/topic/login")
    public void logIntoGame(
            @Payload JoinMessage joinMessage,
            SimpMessageHeaderAccessor headerAccessor
    ){
        String username = joinMessage.getUsername();
        UUID newGUID = UUID.randomUUID();
        playerRepo.addPlayer(newGUID, username);
        log.info("entering function logIntoGame: "+username+" "+newGUID);
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("guid", newGUID);
        messagingTemplate.convertAndSend("/topic/"+username, new GameMessage(MessageType.LOGIN, newGUID, "Logged in"));
        log.info("exiting function logIntoGame: "+username+" "+newGUID);
    }

    @MessageMapping("/topic/lobby")
    public void joinGame(
            @Payload GameMessage gameMessage,
            SimpMessageHeaderAccessor headerAccessor
    ){
        UUID userGUID = gameMessage.getUsername();
        log.info("entering function joinGame: "+userGUID);
        headerAccessor.getSessionAttributes().put("username", userGUID);
        RoomDTO roomDTO = ticTacToeService.chooseRoomForPlayer(userGUID);
        headerAccessor.getSessionAttributes().put("room", roomDTO.getRoomNumber());
        messagingTemplate.convertAndSend("/topic/"+userGUID, roomDTO);
        log.info("exiting function joinGame: "+userGUID);
    }

    @MessageMapping("/room/{roomID}")
    public void handleMove(
            @Payload GameMessage gameMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        int num = Integer.parseInt(headerAccessor.getSessionAttributes().get("room").toString());
//        if (gameMessage.getType()==MessageType.LEAVE){
//            headerAccessor.getSessionAttributes().put("room", null);
//        }
        log.info("gameMess"+gameMessage);
        log.info("user: "+gameMessage.getUsername()+" move: "+gameMessage.getContent());
        log.info("handling move from user {} in room {}", gameMessage.getUsername(), num);
        ticTacToeService.handleMove(num, gameMessage);
    }
}