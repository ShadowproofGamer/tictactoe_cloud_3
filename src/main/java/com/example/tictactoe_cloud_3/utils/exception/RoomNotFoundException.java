package com.example.tictactoe_cloud_3.utils.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RoomNotFoundException extends RuntimeException {

    private final HttpStatus status;

    public RoomNotFoundException() {
        super("Room not found");
        status = HttpStatus.NOT_FOUND;
    }

    public RoomNotFoundException(String message) {
        super(message);
        status = HttpStatus.NOT_FOUND;
    }

}
