package com.example.tictactoe_cloud_3.types;

import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Game {

    private boolean open=true;
    private String player1;
    private String player2;
    private List<Mark> boardState;

    public Game(String player1, String player2){
        this.open=true;
        this.player1=player1;
        this.player2=player2;
        for (int i = 0; i < 9; i++) {
            this.boardState.set(i, Mark.N);
        }
    }

    public void putMove(String player, int cellNumber){
        boardState.set(cellNumber, Objects.equals(player, player1) ? Mark.X : Mark.O);
    }

}
