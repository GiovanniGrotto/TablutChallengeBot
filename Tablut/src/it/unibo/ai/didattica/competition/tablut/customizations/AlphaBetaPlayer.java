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

    final Double MAXVALUE = 100000.0;

    public AlphaBetaPlayer(Game<CustomState, Action, CustomState.Turn> game , double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    //Preso uno stato in input determina se sono avvenute catture rispetto allo stato root
    public double evalCapture(CustomState state, State.Turn player){
        int whitePieces = 0;
        int blackPieces = 0;

        State.Pawn[][] board = state.getBoard();
        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                State.Pawn piece = board[i][j];
                if (Objects.equals(piece, State.Pawn.WHITE)) {
                    whitePieces++;
                }else if (Objects.equals(piece, State.Pawn.BLACK)) {
                    blackPieces++;
                }
            }
        }

        int whitePieceDifference = whitePieces-startWhitePieces;
        int blackPieceDifference = blackPieces-startBlackPieces;
        /*if(whitePieceDifference <-1)
            System.out.print("");
        if(blackPieceDifference<-1)
            System.out.print("");
        if(whitePieceDifference<-1 && blackPieceDifference<-1 && whitePieceDifference!=blackPieceDifference)
            System.out.print("");*/
        if(state.getTurn() == State.Turn.WHITE){
            if(whitePieceDifference < blackPieceDifference)
                return (double) (whitePieceDifference - blackPieceDifference) /this.currDepthLimit;
            else if (whitePieceDifference > blackPieceDifference)
                return (double) -1*(blackPieceDifference - whitePieceDifference) /this.currDepthLimit;
        }else {
            if(whitePieceDifference < blackPieceDifference)
                return (double) -1*(whitePieceDifference - blackPieceDifference)/this.currDepthLimit;
            else if (whitePieceDifference > blackPieceDifference)
                return( double) (blackPieceDifference - whitePieceDifference)/this.currDepthLimit;
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
        double captureEval = evalCapture(state, player);
        super.eval(state, player);
        double eval = this.game.getUtility(state, player);
        /*if((eval == -MAXVALUE || eval == MAXVALUE) && captureEval!=0)
            System.out.print("");*/
        //eval += captureEval;
        if(eval <= -MAXVALUE+10){
            this.foundLoss = true;
        }
        if(eval >= MAXVALUE-10){
            this.foundWin = true;
        }
        this.evalTime += System.currentTimeMillis() - start;
        return eval;
    }

    public void mctsTest(CustomState state){
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 60000;  // 60 seconds in milliseconds
        int gamePlayed = 0;
        Random rand = new Random();

        while (System.currentTimeMillis() < endTime) {
            gamePlayed++;
            CustomState tmpState = state.clone();
            while (tmpState.getTurn()!= State.Turn.WHITEWIN && tmpState.getTurn()!= State.Turn.BLACKWIN){
                if(this.game.getActions(tmpState).size() == 0)
                    System.out.println("TROVATE 0 MOSSE");
                List<Action> actions = this.game.getActions(tmpState);
                tmpState = this.game.getResult(tmpState.clone(), actions.get(rand.nextInt(actions.size())));
            }
        }
        System.out.println("Partite simulate: "+gamePlayed);
    }

    @Override
    public Action makeDecision(CustomState state) {
        long startTime = System.currentTimeMillis();
        this.startWhitePieces = 0;
        this.startBlackPieces = 0;

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

        if(this.foundWin) System.out.println("Trovata vittoria");
        if(this.foundLoss) System.out.println("Trovata sconfitta");
        //Non ho trovato catture o ho trovato vittorie/sconfitte e allora eseguo la migliore mossa secondo l'albero
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH) + " in " + getTimeInSeconds(startTime) +" seconds");
        return a;
    }

    @Override
    protected boolean hasSafeWinner(double resultUtility) {
        /*if(resultUtility == Double.POSITIVE_INFINITY  || resultUtility==Double.POSITIVE_INFINITY-10){
            System.out.println("Found safe winner with eval: "+resultUtility);
            return true;
        }*/
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
