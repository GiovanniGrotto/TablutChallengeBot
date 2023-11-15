package it.unibo.ai.didattica.competition.tablut.customizations;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;


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
        this.counter++;*/
        long start = System.currentTimeMillis();
        super.eval(state, player);
        double eval= this.game.getUtility(state, player);
        this.evalTime += System.currentTimeMillis() - start;
        return eval;
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
        //System.out.println("Depth "+this.currDepthLimit+", time to evaluate states: "+this.evalTime);
        this.game.getInitialState();
        this.evalTime = 0;
    }

    public long getTimeInSeconds(long startTime){
        return (System.currentTimeMillis()-startTime)/1000;
    }
}
