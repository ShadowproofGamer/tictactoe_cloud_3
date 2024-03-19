package com.example.tictactoe_cloud_3.controller;

import com.example.tictactoe_cloud_3.messages.GameMessage;
import com.example.tictactoe_cloud_3.messages.MoveMessage;
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

    @MessageMapping("/topic/lobby")
    public void joinGame(
            @Payload GameMessage gameMessage,
            SimpMessageHeaderAccessor headerAccessor
    ){
        String username = gameMessage.getUsername();
        log.info("entering function joinGame: "+username);
        headerAccessor.getSessionAttributes().put("username", username);
        RoomDTO roomDTO = ticTacToeService.chooseRoomForPlayer(username);
        headerAccessor.getSessionAttributes().put("room", roomDTO.getRoomNumber());
        messagingTemplate.convertAndSend("/topic/"+username, roomDTO);
        log.info("exiting function joinGame: "+username);
    }

    @MessageMapping("/room/{roomID}")
    public void handleMove(
            @Payload GameMessage gameMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        int num = Integer.parseInt(headerAccessor.getSessionAttributes().get("room").toString());
        log.info("gameMess"+gameMessage);
        log.info("user: "+gameMessage.getUsername()+" move: "+gameMessage.getContent());

        log.info("handling move from user {} in room {}", gameMessage.getUsername(), num);
        ticTacToeService.handleMove(num, gameMessage);
    }
    //    @MessageMapping("/topic/{username}")
//    @SendTo("/topic/{username}")
//    public GameMessage handleMove(
//            @Payload GameMessage gameMessage,
//            @PathVariable("username") String username
//    ) {
//        return ticTacToeService.handleMessage(gameMessage);
//    }
}