package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.IOException;
import java.util.*;

public class GameModel implements aima.core.search.adversarial.Game<CustomState, Action, CustomState.Turn>{

    long generateActionsTime = 0;
    final Double MAXVALUE = 100000.0;

    private final LimitedHashMap<String, Double> stateEvaluationMap; //= new LimitedHashMap<>(1000000);
    {
        try {
            stateEvaluationMap = new LimitedHashMap<>(3000000, "stateEvaluation.json", "stateEvaluation");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final LimitedHashMap<String, List<Action>> stateActionsMap; //= new LimitedHashMap<>(1000000);
    {
        try {
            stateActionsMap = new LimitedHashMap<>(2000000, "stateAction.json", "stateAction");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    /*
        Funzione usata per stampare il tempo richiesto per generare le azioni e per valutare le posizioni non viene mai chiamata dall'albero.
    */
    public CustomState getInitialState() {
        System.out.println("Time to generate actions: "+this.generateActionsTime);
        System.out.println("State evaluation map size: "+this.stateEvaluationMap.size()+", state actions map size:"+this.stateActionsMap.size());
        System.out.println();
        this.generateActionsTime = 0;
        return null;
    }

    @Override
    /*
        Funzione usata per scrivere la HashMap degli stati-figli non viene mai chiamata dall'albero.
    */
    public CustomState.Turn[] getPlayers() {
        try {
            //this.stateActionsMap.writeToJsonFile("stateAction.json");
            this.stateEvaluationMap.writeToJsonFile("stateEvaluation.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CustomState.Turn[]{CustomState.Turn.BLACK, CustomState.Turn.WHITE};
    }

    @Override
    public CustomState.Turn getPlayer(CustomState state) {
        return state.getTurn();
    }

    @Override
    public List<Action> getActions(CustomState state) {
        long start = System.currentTimeMillis();
        List<Action> actions = this.stateActionsMap.get(state.toString());
        if (actions != null && actions.size()!=0){
            return actions;
        }
        actions = getActionsBruteForceEarlyStop(state);
        this.stateActionsMap.put(state.toString(), actions);
        this.generateActionsTime += System.currentTimeMillis() - start;
        return actions;
    }

    @Override
    public CustomState getResult(CustomState originalState, Action a) {
        return originalState.getRules().makeMove(originalState.clone(), a);
    }

    @Override
    public boolean isTerminal(CustomState state) {
        return (state.getTurn() == State.Turn.WHITEWIN) || (state.getTurn() == State.Turn.BLACKWIN) || (state.getTurn() == State.Turn.DRAW);
    }

    @Override
    public double getUtility(CustomState state, CustomState.Turn turn) {
        if(this.isTerminal(state)){
            State.Turn winnerPlayer = state.getTurn();
            if (turn == State.Turn.WHITE) {
                if (winnerPlayer.equals(State.Turn.WHITEWIN))
                    return MAXVALUE;
                else if (winnerPlayer.equals(State.Turn.BLACKWIN)){
                    return -MAXVALUE;
                } else {
                    return 0;
                }
            }else if (turn == State.Turn.BLACK) {
                if (winnerPlayer.equals(State.Turn.WHITEWIN))
                    return -MAXVALUE;
                else if (winnerPlayer.equals(State.Turn.BLACKWIN)){
                    return MAXVALUE;
                } else {
                    return 0;
                }
            }
        }

        Double evaluation = this.stateEvaluationMap.get(state.toString());
        if (evaluation != null){
            return evaluation;
        }
        try {
            evaluation = CustomRandomForest.evaluate(state);
            if(state.getTurn() == State.Turn.BLACK) evaluation = 1 - evaluation;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.stateEvaluationMap.put(state.toString(), evaluation);
        return evaluation;
    }

    public String convertToChessPosition(int row, int col) {
        if (row < 1 || row > 9 || col < 1 || col > 9) {
            return "Invalid position";
        }
        char columnChar = (char) ('a' + col-1);
        return columnChar + Integer.toString(row);
    }

    public static int manhattanDistance(int piece1Index, int piece2Index) {
        int size = 9;
        int x1 = piece1Index % size;
        int y1 = piece1Index / size;
        int x2 = piece2Index % size;
        int y2 = piece2Index / size;

        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    // Nettamente più veloce delle altre
    public List<Action> getActionsBruteForceEarlyStop(CustomState state) {
        State.Pawn[][] board = state.getBoard();
        List<Piece> pieces = new ArrayList<>();
        State.Turn turn = state.getTurn();
        Set<Integer> enemyPieceTile = new HashSet<>();
        Set<Integer> playerPieceTile = new HashSet<>();
        Set<Integer> forbiddenTile = new HashSet<>(Arrays.asList(new Integer[]{3, 4, 5, 13, 27, 35, 36, 37, 43, 44, 45, 53, 67, 75, 76, 77, 40}));


        // Scorriamo la board e salviamo in pieces solo i pezzi del colore del turno attuale
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                State.Pawn piece = board[i][j];
                if ((Objects.equals(turn, State.Turn.WHITE) && (Objects.equals(piece, State.Pawn.WHITE) || Objects.equals(piece, State.Pawn.KING)))
                        || (Objects.equals(turn, State.Turn.BLACK) && Objects.equals(piece, State.Pawn.BLACK))) {
                    pieces.add(new Piece(turn.toString(), i, j));
                    playerPieceTile.add(i * 9 + j);
                }else if ((Objects.equals(turn, State.Turn.WHITE) && Objects.equals(piece, State.Pawn.BLACK)) || (Objects.equals(turn, State.Turn.BLACK) && (Objects.equals(piece, State.Pawn.WHITE) || Objects.equals(piece, State.Pawn.KING)))) {
                    enemyPieceTile.add(i * 9 + j);
                }
            }
        }

        List<Action> legalMoves = new ArrayList<>();
        for(Piece piece : pieces) {
            int pieceIndexBeforeMove = piece.row * 9 + piece.col;
            // prendo la posizione da dove parte il pezzo che sto considerando
            String fromTile = convertToChessPosition(piece.row+1, piece.col+1);
            try {
                // Move up
                for (int i = piece.row - 1; i >= 0; i--) {
                    int pieceIndexAfterMove = i * 9 + piece.col;
                    if(forbiddenTile.contains(pieceIndexBeforeMove) && forbiddenTile.contains(pieceIndexAfterMove) && manhattanDistance(pieceIndexBeforeMove, pieceIndexAfterMove) <= 2 && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)){
                        String toTile = convertToChessPosition(i + 1, piece.col + 1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }
                    else if(!forbiddenTile.contains(pieceIndexAfterMove) && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)) {
                        // prendo la posizione dove finirà il pezzo che sto considerando
                        String toTile = convertToChessPosition(i + 1, piece.col + 1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }else break;
                }

                // Move down
                for (int i = piece.row + 1; i < 9; i++) {
                    int pieceIndexAfterMove = i * 9 + piece.col;
                    if(forbiddenTile.contains(pieceIndexBeforeMove) && forbiddenTile.contains(pieceIndexAfterMove) && manhattanDistance(pieceIndexBeforeMove, pieceIndexAfterMove) <= 2 && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)){
                        String toTile = convertToChessPosition(i+1, piece.col+1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }
                    else if(!forbiddenTile.contains(pieceIndexAfterMove) && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)) {
                        String toTile = convertToChessPosition(i+1, piece.col+1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }else break;
                }

                // Move left
                for (int j = piece.col - 1; j >= 0; j--) {
                    int pieceIndexAfterMove = piece.row * 9 + j;
                    if(forbiddenTile.contains(pieceIndexBeforeMove) && forbiddenTile.contains(pieceIndexAfterMove) && manhattanDistance(pieceIndexBeforeMove, pieceIndexAfterMove) <= 2 && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)){
                        String toTile = convertToChessPosition(piece.row+1, j+1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }
                    else if(!forbiddenTile.contains(pieceIndexAfterMove) && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)) {
                        String toTile = convertToChessPosition(piece.row+1, j+1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }else break;
                }

                // Move right
                for (int j = piece.col + 1; j < 9; j++) {
                    int pieceIndexAfterMove = piece.row * 9 + j;
                    if(forbiddenTile.contains(pieceIndexBeforeMove) && forbiddenTile.contains(pieceIndexAfterMove) && manhattanDistance(pieceIndexBeforeMove, pieceIndexAfterMove) <= 2 && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)){
                        String toTile = convertToChessPosition(piece.row+1, j+1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }
                    else if(!forbiddenTile.contains(pieceIndexAfterMove) && !enemyPieceTile.contains(pieceIndexAfterMove) && !playerPieceTile.contains(pieceIndexAfterMove)) {
                        String toTile = convertToChessPosition(piece.row+1, j+1);
                        legalMoves.add(new Action(fromTile, toTile, turn));
                    }else break;
                }
            }catch (Exception e){
                //e.printStackTrace();
            }
        }
        /*List<Action> actuallyLegalMoves = this.getActionsBruteForce(state);
        List<Action> differentMove = new ArrayList<>();
        if(legalMoves.size() != actuallyLegalMoves.size()) {
            for(Action trueMove : actuallyLegalMoves){
                boolean flag = false;
                for(Action legalMove : legalMoves){
                    if(Objects.equals(legalMove.toString(), trueMove.toString())){
                        flag = true;
                    }
                }
                if(!flag)
                    differentMove.add(trueMove);
            }
        }
        if(differentMove.size()!=0)
            System.out.println("TROVATE DIFFERENZE NELLE MOSSE");*/
        return legalMoves;
    }

    public List<Action> getActionsBruteForce(CustomState state) {
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
        List<Action> allMoves = new ArrayList<>();
        State.Turn turn = state.getTurn();
        for(Piece piece : pieces) {
            // prendo la posizione da dove parte il pezzo che sto considerando
            String fromTile = convertToChessPosition(piece.row+1, piece.col+1);
            try {
                // Move up
                for (int i = piece.row - 1; i >= 0; i--) {
                    // prendo la posizione dove finirà il pezzo che sto considerando
                    String toTile = convertToChessPosition(i+1, piece.col+1);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }

                // Move down
                for (int i = piece.row + 1; i < 9; i++) {
                    String toTile = convertToChessPosition(i+1, piece.col+1);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }

                // Move left
                for (int j = piece.col - 1; j >= 0; j--) {
                    String toTile = convertToChessPosition(piece.row+1, j+1);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }

                // Move right
                for (int j = piece.col + 1; j < 9; j++) {
                    String toTile = convertToChessPosition(piece.row+1, j+1);
                    allMoves.add(new Action(fromTile, toTile, turn));
                }
            }catch (Exception e){
                //e.printStackTrace();
            }
        }

        //passo ogni azione per checkmove, se non ritorna eccezioni è legale e la aggiungo alla lista di mosse legali
        List<Action> legalMoves = new ArrayList<>();
        for(Action action: allMoves){
            try{
                state.getRules().checkMove(state.clone(), action);
                legalMoves.add(action);
            }catch (Exception e){
                //e.printStackTrace();
            }
        }
        return legalMoves;
    }
}
