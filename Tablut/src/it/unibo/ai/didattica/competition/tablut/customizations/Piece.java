package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class Piece{
    String color;
    int row;
    int col;

    public Piece(String color,int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }
}