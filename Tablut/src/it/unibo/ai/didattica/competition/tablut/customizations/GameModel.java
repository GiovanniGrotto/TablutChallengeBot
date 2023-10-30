package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.customizations.CustomState;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameModel implements aima.core.search.adversarial.Game<CustomState, Action, CustomState.Turn>{

    @Override
    public CustomState getInitialState() {
        return null;
    }

    @Override
    public CustomState.Turn[] getPlayers() {
        return new CustomState.Turn[]{CustomState.Turn.BLACK, CustomState.Turn.WHITE};
    }

    @Override
    public CustomState.Turn getPlayer(CustomState state) {
        return state.getTurn();
    }

    @Override
    public List<Action> getActions(CustomState state) {
        State.Pawn[][] board = state.getBoard();
        List<Piece> pieces = new ArrayList<Piece>();
        String turn = state.getTurn().toString();
        // Scorriamo la board e salviamo in pieces solo i pezzi del colore del turno attuale
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                String piece = board[i][j].toString();
                if((Objects.equals(turn, "W") && (Objects.equals(piece, "K") || Objects.equals(piece, "W"))) || (Objects.equals(turn, "B") && Objects.equals(piece, "B"))){
                    pieces.add(new Piece(turn, i, j));
                }
            }
        }
        // calcolo le mosse che può fare ogni pezzo e genero altri pezzi lì
        List<Piece> moves = new ArrayList<>();
        for(Piece piece : pieces) {
            // Move up
            for (int i = piece.row - 1; i >= 0; i--) {
                moves.add(new Piece(turn, i, piece.col));
            }

            // Move down
            for (int i = piece.row + 1; i < 9; i++) {
                moves.add(new Piece(turn, i, piece.col));
            }

            // Move left
            for (int j = piece.col - 1; j >= 0; j--) {
                moves.add(new Piece(turn, piece.row, j));
            }

            // Move right
            for (int j = piece.col + 1; j < 9; j++) {
                moves.add(new Piece(turn, piece.row, j));
            }
        }
        return null;
    }

    @Override
    public CustomState getResult(CustomState state, Action action) {
        try {
            return (CustomState) state.getRules().checkMove(state, action);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isTerminal(CustomState state) {
        return (state.getTurn() == State.Turn.WHITEWIN) || (state.getTurn() == State.Turn.BLACKWIN) || (state.getTurn() == State.Turn.DRAW);
    }

    @Override
    public double getUtility(CustomState state, CustomState.Turn turn) {
        return 0;
    }
}
