package it.unibo.ai.didattica.competition.tablut.customizations;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class AlphaBetaPlayer extends IterativeDeepeningAlphaBetaSearch<CustomState, Action, CustomState.Turn> {

    long evalTime = 0;
    int counter = 0;
    int startWhitePieces = 0;
    int startBlackPieces = 0;
    Boolean foundCapture = false;

    public AlphaBetaPlayer(Game<CustomState, Action, CustomState.Turn> game , double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    public double evalCapture(CustomState state, State.Turn turn){
        Integer whitePieces = 0;
        Integer blackPieces = 0;
        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                State.Pawn piece = board[i][j];
                if (Objects.equals(piece, State.Pawn.WHITE)) {
                    whitePieces++;
                }else if (Objects.equals(piece, State.Pawn.BLACK)) {
                    blackPieces++;
                }
            }
        }
        if(turn == State.Turn.WHITE){
            if(whitePieces-startWhitePieces > blackPieces-startBlackPieces)
                return 1.1;
            else if (whitePieces-startWhitePieces < blackPieces-startBlackPieces)
                return -1.1;
        }else {
            if(whitePieces-startWhitePieces > blackPieces-startBlackPieces)
                return -1.1;
            else if (whitePieces-startWhitePieces < blackPieces-startBlackPieces)
                return +1.1;
        }
        return 0;
    }

    @Override
    public double eval(CustomState state, State.Turn player) {
        /*if(this.counter%100000==0){ // ogni 100000 evaluation sovrascrive la hashmap local con quella aggiornata
            this.game.getPlayers();
        }*/
        this.counter++;
        long start = System.currentTimeMillis();
        double eval = evalCapture(state, player);
        if(this.currDepthLimit == 1 && (eval==1.1 || eval == -1.1)){
            if(eval == 1.1)
                this.foundCapture = true;
            return eval;
        }
        super.eval(state, player);
        eval= this.game.getUtility(state, player);
        this.evalTime += System.currentTimeMillis() - start;
        return eval;
    }

    @Override
    public Action makeDecision(CustomState state) {
        this.startWhitePieces = 0;
        this.startBlackPieces = 0;
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
        //this.game.getPlayers();
        long startTime = System.currentTimeMillis();
        Action a = super.makeDecision(state);
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH) + " in " + getTimeInSeconds(startTime) +" seconds");
        return a;
    }

    @Override
    protected boolean hasSafeWinner(double resultUtility) {
        if(this.foundCapture){
            this.foundCapture = false;
            return true;
        }
        return false;
    }

    @Override
    protected void incrementDepthLimit() {
        ++this.currDepthLimit;
        System.out.println("Depth "+this.currDepthLimit+", time to evaluate states: "+this.evalTime);
        this.game.getInitialState();
        this.evalTime = 0;
    }

    public long getTimeInSeconds(long startTime){
        return (System.currentTimeMillis()-startTime)/1000;
    }
}
