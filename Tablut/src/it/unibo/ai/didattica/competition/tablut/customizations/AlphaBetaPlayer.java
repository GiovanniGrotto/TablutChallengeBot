package it.unibo.ai.didattica.competition.tablut.customizations;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.*;

import static java.lang.Math.min;


public class AlphaBetaPlayer extends IterativeDeepeningAlphaBetaSearch<CustomState, Action, CustomState.Turn> {

    long evalTime = 0;
    int counter = 0;

    public AlphaBetaPlayer(Game<CustomState, Action, CustomState.Turn> game , double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    @Override
    public double eval(CustomState state, State.Turn player) {
        /*if(this.counter%100000==0){ // ogni 100000 evaluation sovrascrive la hashmap local con quella aggiornata
            this.game.getPlayers();
        }
        this.counter++;
        long start = System.currentTimeMillis();
        super.eval(state, player);
        double eval= this.game.getUtility(state, player);
        this.evalTime += System.currentTimeMillis() - start;
        return eval;*/
        super.eval(state, player);
        return heuristicEvaluation(state);
    }

    @Override
    public Action makeDecision(CustomState state) {
        //this.game.getPlayers();
        long startTime = System.currentTimeMillis();
        Action a = super.makeDecision(state);
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH) + " in " + getTimeInSeconds(startTime) +" seconds");
        return a;
    }

    @Override
    protected boolean hasSafeWinner(double resultUtility) {
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

    public int heuristicEvaluation(CustomState state){
        Integer whitePieces = 0;
        Integer blackPieces = 0;
        List<Integer> escapeTileX = new ArrayList<>(Arrays.asList(0, 1, 7, 8, 0, 8, 0, 8, 0, 1, 7, 8));
        List<Integer> escapeTileY = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 1, 1, 7, 7, 8, 8, 8, 8));
        Integer kingX = 0;
        Integer kingY = 0;
        Integer minEscapeDistance = 100;


        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                State.Pawn piece = state.getBoard()[i][j];
                if(piece == State.Pawn.WHITE) whitePieces++;
                if(piece == State.Pawn.BLACK) blackPieces++;
                if(piece == State.Pawn.KING){
                    kingX = i;
                    kingY = j;
                }
            }
        }

        for(int i = 0; i < escapeTileX.size(); i++) {
            minEscapeDistance = Math.min(minEscapeDistance, Math.abs(kingX - escapeTileX.get(i)) + Math.abs(kingY - escapeTileY.get(i)));
        }

        return whitePieces-blackPieces+(7-minEscapeDistance);
    }
}
