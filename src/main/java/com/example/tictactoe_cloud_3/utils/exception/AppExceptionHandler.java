package com.example.tictactoe_cloud_3.utils.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> roomNotFoundException(RoomNotFoundException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

    @ExceptionHandler(RoomIsEmpty.class)
    public ResponseEntity<String> roomIsEmpty(RoomIsEmpty ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

}
