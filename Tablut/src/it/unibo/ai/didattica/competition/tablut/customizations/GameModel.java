package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameModel implements aima.core.search.adversarial.Game<CustomState, Action, CustomState.Turn>{

    public String convertToChessPosition(int row, int col) {
        if (row < 1 || row > 9 || col < 1 || col > 9) {
            return "Invalid position";
        }
        char columnChar = (char) ('a' + col - 1);
        return columnChar + Integer.toString(row);
    }

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
        List<Piece> pieces = new ArrayList<>();
        String turnString = state.getTurn().toString();

        // Scorriamo la board e salviamo in pieces solo i pezzi del colore del turno attuale
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                String piece = board[i][j].toString();
                if((Objects.equals(turnString, "W") && (Objects.equals(piece, "K") || Objects.equals(piece, "W"))) || (Objects.equals(turnString, "B") && Objects.equals(piece, "B"))){
                    pieces.add(new Piece(turnString, i, j));
                }
            }
        }

        // calcolo le mosse che può fare ogni pezzo e genero altri pezzi lì
        // TODO: controllare che convertChessPosition funzioni bene e che anche i for genera mosse usino i,j bene e che siano passati bene quando si crea new Action
        List<Action> allMoves = new ArrayList<>();
        for(Piece piece : pieces) {
            String fromTile = convertToChessPosition(piece.row, piece.col);
            State.Turn turn = state.getTurn();
            try {
                // Move up
                for (int i = piece.row - 1; i >= 0; i--) {
                    String toTile = convertToChessPosition(i, piece.col);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }

                // Move down
                for (int i = piece.row + 1; i < 9; i++) {
                    String toTile = convertToChessPosition(i, piece.col);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }

                // Move left
                for (int j = piece.col - 1; j >= 0; j--) {
                    String toTile = convertToChessPosition(piece.row, j);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }

                // Move right
                for (int j = piece.col + 1; j < 9; j++) {
                    String toTile = convertToChessPosition(piece.row, j);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //passo ogni azione per checkmove, se non ritorna eccezioni è legale e la aggiungo alla lista di mosse legali
        List<Action> legalMoves = new ArrayList<>();
        for(Action action: allMoves){
            try{
                state.getRules().checkMove(state, action);
                legalMoves.add(action);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return legalMoves;
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
