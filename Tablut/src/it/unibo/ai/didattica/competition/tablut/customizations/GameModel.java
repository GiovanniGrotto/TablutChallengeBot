package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameModel implements aima.core.search.adversarial.Game<CustomState, Action, CustomState.Turn>{

    long generateActionsTime = 0;
    long generateResultsTime = 0;
    long randomForestTime = 0;
    int evaluationMapHit = 0;
    int evaluationMapFails = 0;

    final Double MAXVALUE = 100000.0;

    private final LimitedHashMap<String, Double> stateEvaluationMap; //= new LimitedHashMap<>(1000000);
    {
        try {
            stateEvaluationMap = new LimitedHashMap<>(2000000, System.getProperty("user.dir")+ File.separator + "stateEvaluation.json", "stateEvaluation");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private final LimitedHashMap<String, CustomState> actionResultMap = new LimitedHashMap<>(500000);


    @Override
    /*
        Funzione usata per stampare il tempo richiesto per generare le azioni e per valutare le posizioni non viene mai chiamata dall'albero.
    */
    public CustomState getInitialState() {
        System.out.println("Time to generate actions: "+this.generateActionsTime);
        System.out.println("Time to get results: "+this.generateResultsTime);
        System.out.println("Eval map hitted: "+this.evaluationMapHit+" times, failed: "+this.evaluationMapFails+" times");
        System.out.println("Time consumed by random forest: "+this.randomForestTime);
        //System.out.println("State evaluation map size: "+this.stateEvaluationMap.size()+", state actions map size:"+this.stateActionsMap.size());
        //System.out.println();
        this.generateActionsTime = 0;
        this.generateResultsTime = 0;
        this.evaluationMapFails = 0;
        this.evaluationMapHit = 0;
        this.randomForestTime = 0;
        return null;
    }

    @Override
    /*
        Funzione usata per scrivere la HashMap degli stati-figli non viene mai chiamata dall'albero.
    */
    public CustomState.Turn[] getPlayers() {
        actionResultMap.clear();
        /*try {
            //this.stateActionsMap.writeToJsonFile("stateAction.json");
            this.stateEvaluationMap.writeToJsonFile("stateEvaluation.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        return new CustomState.Turn[]{CustomState.Turn.BLACK, CustomState.Turn.WHITE};
    }

    @Override
    public CustomState.Turn getPlayer(CustomState state) {
        return state.getTurn();
    }

    @Override
    public List<Action> getActions(CustomState state) {
        long start = System.currentTimeMillis();
        List<Action>actions = getActionsBruteForceEarlyStop(state);
        this.generateActionsTime += System.currentTimeMillis() - start;
        return actions;
    }

    @Override
    public CustomState getResult(CustomState originalState, Action a) {
        long start = System.currentTimeMillis();
        String stateAction = originalState.toString() + a.toString();
        CustomState newState = this.actionResultMap.get(stateAction);
        if (newState != null){
            return newState;
        }
        newState = originalState.getRules().makeMove(originalState.clone(), a);
        this.actionResultMap.put(stateAction, newState.clone());
        this.generateResultsTime += System.currentTimeMillis() - start;
        return newState;
        /*long start = System.currentTimeMillis();
        CustomState newState = originalState.getRules().makeMove(originalState.clone(), a);
        this.generateResultsTime += System.currentTimeMillis() - start;
        return newState;*/
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
            this.evaluationMapHit++;
            return evaluation;
        }
        this.evaluationMapFails++;
        long start = System.currentTimeMillis();
        try {
            evaluation = CustomRandomForest.evaluate(state);
            if(state.getTurn() == State.Turn.BLACK) evaluation = 1 - evaluation;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.stateEvaluationMap.put(state.toString(), evaluation);
        this.randomForestTime += System.currentTimeMillis() - start;
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
        return legalMoves;
    }
}
