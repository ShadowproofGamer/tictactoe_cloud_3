package com.example.tictactoe_cloud_3.service;

import com.example.tictactoe_cloud_3.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final RoomRepo roomRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;

    //TODO - Sharing game state, handling moves etc.





}
