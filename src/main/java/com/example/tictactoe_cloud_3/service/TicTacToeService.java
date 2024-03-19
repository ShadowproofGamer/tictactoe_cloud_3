package com.example.tictactoe_cloud_3.service;


import com.example.tictactoe_cloud_3.messages.*;
import com.example.tictactoe_cloud_3.repo.RoomRepo;
import com.example.tictactoe_cloud_3.types.*;
import com.example.tictactoe_cloud_3.utils.exception.RoomIsEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class TicTacToeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomRepo roomRepo;


    public TicTacToeService(SimpMessagingTemplate messagingTemplate, RoomRepo roomRepo) {
        this.messagingTemplate = messagingTemplate;
        this.roomRepo = roomRepo;
    }

    public RoomDTO chooseRoomForPlayer(String playerName) {

        Room room;
        Optional<Room> optionalRoom = roomRepo.getRoomWithOneFreeSlot();
        if (optionalRoom.isPresent()) {
            room = optionalRoom.get();
            room.setPlayer2(playerName);
            room.setFreeSlots(0);
            chooseStartingPlayer(room);
            log.info("new game: " + room.getPlayerStarting());
            sendStartGameMessage(room);
            return RoomDTO.of(room);
        }

        Optional<Room> optionalRoom2 = roomRepo.getRoomWithTwoFreeSlots();
        if (optionalRoom2.isPresent()) {
            room = optionalRoom2.get();
            room.setPlayer1(playerName);
            room.setFreeSlots(1);
            return RoomDTO.of(room);
        }

        Room newRoom = new Room(roomRepo.getRoomCounter());
        roomRepo.setRoomCounter(roomRepo.getRoomCounter() + 1);
        newRoom.setPlayer1(playerName);
        newRoom.setFreeSlots(1);
        roomRepo.addRoom(newRoom);
        return RoomDTO.of(newRoom);

    }

    private void chooseStartingPlayer(Room room) {
        boolean isStarting = new Random().nextBoolean();
        String starter = isStarting ? room.getPlayer1() : room.getPlayer2();
        room.setPlayerStarting(starter);
    }

    private void sendStartGameMessage(Room room) {
        sendStartGameMessageToPlayer(room.getPlayer1(), room);
        sendStartGameMessageToPlayer(room.getPlayer2(), room);
    }

    private void sendStartGameMessageToPlayer(String playerName, Room room) {
        log.info("Sending starting game info to /topic/" + playerName);
        messagingTemplate.convertAndSend("/topic/" + playerName, RoomDTO.of(room));
        messagingTemplate.convertAndSend("/topic/" + playerName, new GameStartMessage(playerName));
    }

    public void deletePlayerFromRoom(int roomNumber, String playerName) {
        log.info("deleting user {} from room {}", playerName, roomNumber);
        Room room = roomRepo.getRoomByName(roomNumber);
        if (room.getFreeSlots() == 2) {
            throw new RoomIsEmpty();
        }
        if (room.getFreeSlots() == 1) {
            roomRepo.removeRoom(room);
        } else {
            if (room.getPlayer1().equals(playerName)) {
                room.setPlayer1(room.getPlayer2());
            }
            room.setPlayer2(null);
            room.setFreeSlots(1);
            messagingTemplate.convertAndSend("/topic/" + room.getPlayer1(), new OpponentLeftMessage(playerName));
        }
    }

    public RoomDTO getRoomDTO(int roomNumber) {
        return RoomDTO.of(roomRepo.getRoomByName(roomNumber));
    }

    public Room getRoom(int roomNumber) {
        return roomRepo.getRoomByName(roomNumber);
    }


    public void handleMove(int roomNumber, GameMessage gameMessage) {
        String player = gameMessage.getUsername();
        if (gameMessage.getType() == MessageType.MOVE) {
            int move = Integer.parseInt(gameMessage.getContent());
            messagingTemplate.convertAndSend("/room/" + roomNumber, new MoveMessage(player, move));
        } else if (gameMessage.getType() == MessageType.LEAVE) {
            deletePlayerFromRoom(roomNumber, gameMessage.getUsername());
        } else {
            messagingTemplate.convertAndSend("/topic/" + player, new GameMessage(MessageType.RESPONSEERROR, player, "Wrong data"));
        }
    }

}