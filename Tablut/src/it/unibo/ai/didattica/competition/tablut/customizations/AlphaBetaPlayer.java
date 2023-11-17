package it.unibo.ai.didattica.competition.tablut.customizations;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.*;


public class AlphaBetaPlayer extends IterativeDeepeningAlphaBetaSearch<CustomState, Action, CustomState.Turn> {

    long evalTime = 0;
    int counter = 0;
    int startWhitePieces = 0;
    int startBlackPieces = 0;
    Boolean foundWin = false;
    Boolean foundLoss = false;
    Set<String> captureStates = new HashSet<>();
    int kingTile = -1;
    Set<Integer> whitePiecesSet = new HashSet<>();
    Set<Integer> blackPiecesSet = new HashSet<>();
    Set<Integer> rowIntestingTile = new HashSet<>(Arrays.asList(new Integer[]{3, 4, 5, 13, 27, 35, 36, 37, 43, 44, 45, 53, 67, 75, 76, 77, 40}));
    Set<Integer> colIntestingTile = new HashSet<>(Arrays.asList(new Integer[]{3, 4, 5, 13, 27, 35, 36, 37, 43, 44, 45, 53, 67, 75, 76, 77, 40}));
    Set<Integer> bothIntestingTile = new HashSet<>(Arrays.asList(new Integer[]{3, 4, 5, 13, 27, 35, 36, 37, 43, 44, 45, 53, 67, 75, 76, 77, 40}));



    public AlphaBetaPlayer(Game<CustomState, Action, CustomState.Turn> game , double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    //Preso uno stato in input determina se sono avvenute catture rispetto allo stato root
    public double evalCapture(CustomState state, State.Turn turn){
        int whitePieces = 0;
        int blackPieces = 0;
        whitePiecesSet.clear();
        blackPiecesSet.clear();

        State.Pawn[][] board = state.getBoard();
        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                State.Pawn piece = board[i][j];
                if (Objects.equals(piece, State.Pawn.WHITE)) {
                    whitePieces++;
                    this.whitePiecesSet.add(i * 9 + j);
                }else if (Objects.equals(piece, State.Pawn.BLACK)) {
                    blackPieces++;
                    this.blackPiecesSet.add(i * 9 + j);
                }
                else if(Objects.equals(piece, State.Pawn.KING)){
                    this.kingTile = i * 9 + j;
                }
            }
        }

        int whitePieceDifference = whitePieces-startWhitePieces;
        int blackPieceDifference = blackPieces-startBlackPieces;
        if(state.getTurn() == State.Turn.WHITE){
            if(whitePieceDifference > blackPieceDifference)
                return (double) (-1 * blackPieceDifference) /this.currDepthLimit;
            else if (whitePieceDifference < blackPieceDifference)
                return (double) whitePieceDifference /this.currDepthLimit;
        }else {
            if(whitePieceDifference > blackPieceDifference)
                return (double) (-1 * whitePieceDifference)/this.currDepthLimit;
            else if (whitePieceDifference < blackPieceDifference)
                return( double) (blackPieceDifference)/this.currDepthLimit;
        }
        return 0;
    }

    //Given a state determine it will set foundWinLoss to true
    public boolean checkDoubleTreats(State.Turn player){
        if(this.rowIntestingTile.contains(kingTile)){
            int kingRow = kingTile / 9;
            if(player == State.Turn.WHITE) {
                for(int enemyTile : blackPiecesSet){
                    int enemyRow = enemyTile / 9;
                    if (kingRow == enemyRow) {
                        return false; // King is aligned in the same row or col with an enemy piece
                    }
                }
            }else{
                for(int enemyTile : whitePiecesSet){
                    int enemyRow = enemyTile / 9;
                    if (kingRow == enemyRow) {
                        return false; // King is aligned in the same row or col with an enemy piece
                    }
                }
            }
            return true;
        }else if(this.colIntestingTile.contains(kingTile)) {
            int kingCol = kingTile % 9;
            if (player == State.Turn.WHITE) {
                for (int enemyTile : blackPiecesSet) {
                    int enemyCol = enemyTile % 9;
                    if (kingCol == enemyCol) {
                        return false; // King is aligned in the same row or col with an enemy piece
                    }
                }
            } else {
                for (int enemyTile : whitePiecesSet) {
                    int enemyCol = enemyTile % 9;
                    if (kingCol == enemyCol) {
                        return false; // King is aligned in the same row or col with an enemy piece
                    }
                }
            }
            return true;
        }/*else if (this.bothIntestingTile.contains(kingTile)) {
            int kingRow = kingTile / 9;
            int  kingCol = kingTile % 9;
            if (player == State.Turn.WHITE) {
                for (int enemyTile : blackPiecesSet) {
                    int enemyRol = enemyTile / 9;
                    int enemyCol = enemyTile % 9;
                    if (kingCol == enemyCol) {
                        return false; // King is aligned in the same row or col with an enemy piece
                    }
                }
            } else {
                int obstacol = 0;
                for (int enemyTile : whitePiecesSet) {
                    int kingRow = kingTile / 9;
                    int enemyCol = enemyTile % 9;
                    if (kingCol == enemyCol) {
                        return false; // King is aligned in the same row or col with an enemy piece
                    }
                }
            }
            return true;
        }*/
        return false;
    }

    @Override
    public double eval(CustomState state, State.Turn player) {
        if(this.counter%100000==0){ // ogni 100000 evaluation sovrascrive la hashmap local con quella aggiornata
            this.game.getPlayers();
        }
        this.counter++;
        long start = System.currentTimeMillis();
        double captureEval = evalCapture(state, player);
        /*if(this.currDepthLimit == 1 && (captureEval==1.1 || captureEval == -1.1)){
            if(captureEval == 1.1)
                this.captureStates.add(state.toString());
        }*/
        super.eval(state, player);
        double eval = this.game.getUtility(state, player) + captureEval;
        /*if(checkDoubleTreats(player)) {
            if (player == State.Turn.WHITE)
                eval = Double.POSITIVE_INFINITY - 10;
            else
                eval = Double.NEGATIVE_INFINITY + 10;
        }*/
        if(this.currDepthLimit <= 3 && (eval == Double.NEGATIVE_INFINITY || eval == Double.NEGATIVE_INFINITY+10)){
            this.foundLoss = true;
        }
        if(eval == Double.POSITIVE_INFINITY || eval == Double.POSITIVE_INFINITY-10){
            this.foundWin = true;
        }
        this.evalTime += System.currentTimeMillis() - start;
        return eval;
    }

    @Override
    public Action makeDecision(CustomState state) {
        long startTime = System.currentTimeMillis();
        this.foundWin = false;
        this.foundLoss = false;
        this.startWhitePieces = 0;
        this.startBlackPieces = 0;
        this.captureStates.clear();

        //Annota quanti pezzi per ogni colore ci sono nella scacchiera
        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                State.Pawn piece = board[i][j];
                if (Objects.equals(piece, State.Pawn.WHITE)) {
                    startWhitePieces++;
                }else if (Objects.equals(piece, State.Pawn.BLACK)) {
                    startBlackPieces++;
                }
            }
        }

        //L'albero inizia la computazione
        Action a = super.makeDecision(state);

        //Se ho catture e non ho trovato vittorie/sconfitte allora faccio la cattura
        if(!this.captureStates.isEmpty() && !this.foundWin && !this.foundLoss){
            List<Action> actions = this.game.getActions(state);
            for(Action action : actions){
                CustomState tmpState = this.game.getResult(state.clone(), action);
                if(this.captureStates.contains(tmpState.toString())) {
                    System.out.println("Mangio un pezzo");
                    System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH) + " in " + getTimeInSeconds(startTime) + " seconds");
                    return action;
                }
            }
        }

        if(this.foundWin) System.out.println("Trovata vittoria");
        if(this.foundLoss) System.out.println("Trovata sconfitta");
        //Non ho trovato catture o ho trovato vittorie/sconfitte e allora eseguo la migliore mossa secondo l'albero
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH) + " in " + getTimeInSeconds(startTime) +" seconds");
        return a;
    }

    @Override
    protected boolean hasSafeWinner(double resultUtility) {
        if(resultUtility == Double.POSITIVE_INFINITY  || resultUtility==Double.POSITIVE_INFINITY-10){
            System.out.println("Found safe winner with eval: "+resultUtility);
            return true;
        }
        return false;
    }

    @Override
    protected void incrementDepthLimit() {
        ++this.currDepthLimit;
        //System.out.println("Depth "+this.currDepthLimit+", time to evaluate states: "+this.evalTime);
        this.game.getInitialState();
        this.evalTime = 0;
    }

    public long getTimeInSeconds(long startTime){
        return (System.currentTimeMillis()-startTime)/1000;
    }
}
