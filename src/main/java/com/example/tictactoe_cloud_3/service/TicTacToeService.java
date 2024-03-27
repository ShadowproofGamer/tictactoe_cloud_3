package com.example.tictactoe_cloud_3.service;


import com.example.tictactoe_cloud_3.messages.*;
import com.example.tictactoe_cloud_3.repo.PlayerRepo;
import com.example.tictactoe_cloud_3.repo.RoomRepo;
import com.example.tictactoe_cloud_3.types.*;
import com.example.tictactoe_cloud_3.utils.exception.RoomIsEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TicTacToeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomRepo roomRepo;
    private final PlayerRepo playerRepo;


    public TicTacToeService(SimpMessagingTemplate messagingTemplate, RoomRepo roomRepo, PlayerRepo playerRepo) {
        this.messagingTemplate = messagingTemplate;
        this.roomRepo = roomRepo;
        this.playerRepo = playerRepo;
    }

    public RoomDTO chooseRoomForPlayer(UUID playerGUID) {

        Room room;
        Optional<Room> optionalRoom = roomRepo.getRoomWithOneFreeSlot();
        if (optionalRoom.isPresent()) {
            room = optionalRoom.get();
            room.setPlayer2(playerGUID);
            room.setFreeSlots(0);
            chooseStartingPlayer(room);
            log.info("new game: " + room.getPlayerStarting());
            sendStartGameMessage(room);
            return RoomDTO.of(room);
        }

        Optional<Room> optionalRoom2 = roomRepo.getRoomWithTwoFreeSlots();
        if (optionalRoom2.isPresent()) {
            room = optionalRoom2.get();
            room.setPlayer1(playerGUID);
            room.setFreeSlots(1);
            return RoomDTO.of(room);
        }

        Room newRoom = new Room(roomRepo.getRoomCounter());
        roomRepo.setRoomCounter(roomRepo.getRoomCounter() + 1);
        newRoom.setPlayer1(playerGUID);
        newRoom.setFreeSlots(1);
        roomRepo.addRoom(newRoom);
        return RoomDTO.of(newRoom);

    }

    private void chooseStartingPlayer(Room room) {
        boolean isStarting = new Random().nextBoolean();
        UUID starter = isStarting ? room.getPlayer1() : room.getPlayer2();
        room.setPlayerStarting(starter);
    }

    private void sendStartGameMessage(Room room) {
        sendStartGameMessageToPlayer(room.getPlayer1(), room);
        sendStartGameMessageToPlayer(room.getPlayer2(), room);
    }

    private void sendStartGameMessageToPlayer(UUID playerGUID, Room room) {
        log.info("Sending starting game info to /topic/" + playerGUID);
        messagingTemplate.convertAndSend("/topic/" + playerGUID, RoomDTO.of(room));
//        messagingTemplate.convertAndSend("/topic/" + playerGUID, new GameStartMessage(playerGUID));
    }

    public void deletePlayerFromRoom(int roomNumber, UUID playerGUID) {
        log.info("deleting user {} from room {}", playerGUID, roomNumber);
        Room room = roomRepo.getRoomByName(roomNumber);
        if (room.getFreeSlots() == 2) {
            throw new RoomIsEmpty();
        }
        if (room.getFreeSlots() == 1) {
            roomRepo.removeRoom(room);
        } else {
            if (room.getPlayer1().equals(playerGUID)) {
                room.setPlayer1(room.getPlayer2());
            }
            room.setPlayer2(null);
            room.setFreeSlots(1);
            roomRepo.removeRoom(room);
        }
    }


    public RoomDTO getRoomDTO(int roomNumber) {
        return RoomDTO.of(roomRepo.getRoomByName(roomNumber));
    }

    public Room getRoom(int roomNumber) {
        return roomRepo.getRoomByName(roomNumber);
    }


    public void handleMove(int roomNumber, GameMessage gameMessage) {
        log.info("service handleMove");
        UUID player = gameMessage.getUsername();
        if (gameMessage.getType() == MessageType.MOVE) {
            String move = gameMessage.getContent();
            log.info("service handles move of {}", gameMessage.getUsername());
            messagingTemplate.convertAndSend("/topic/room/" + roomNumber, new GameMessage(MessageType.MOVE, player, String.valueOf(move)));
        } else if (gameMessage.getType() == MessageType.LEAVE) {
            log.info("handled leave of {}", gameMessage.getUsername());
            UUID player1 = getRoom(roomNumber).getPlayer1();
            UUID player2 = getRoom(roomNumber).getPlayer2();
            UUID playerSend = Objects.equals(gameMessage.getUsername(), player1)?player2:player1;
            messagingTemplate.convertAndSend("/topic/room/"+roomNumber, new GameMessage(MessageType.LEAVE, playerSend, "Opponent left"));
            deletePlayerFromRoom(roomNumber, playerSend);

        } else {
            log.info("wrong move of {}", gameMessage.getUsername());
            messagingTemplate.convertAndSend("/topic/" + player, new GameMessage(MessageType.RESPONSEERROR, player, "Wrong data"));
        }
    }

}