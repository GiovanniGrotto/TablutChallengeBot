package it.unibo.ai.didattica.competition.tablut.customizations;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class AlphaBetaPlayer extends IterativeDeepeningAlphaBetaSearch<CustomState, Action, CustomState.Turn> {

    long evalTime = 0;
    int counter = 0;
    int startWhitePieces = 0;
    int startBlackPieces = 0;
    Boolean foundWinLoss = false;
    Set<String> captureStates = new HashSet<>();


    public AlphaBetaPlayer(Game<CustomState, Action, CustomState.Turn> game , double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    //Preso uno stato in input determina se sono avvenute catture rispetto allo stato root
    public double evalCapture(CustomState state, State.Turn turn){
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

    //Given a state determine it will set foundWinLoss to true
    /*public void isWinLoss(CustomState state, State.Turn turn){
        State.Turn winnerPlayer = state.getTurn();
        if (turn == State.Turn.WHITE) {
            if (winnerPlayer.equals(State.Turn.WHITEWIN))
                this.foundWinLoss = true;
            else if (winnerPlayer.equals(State.Turn.BLACKWIN)){
                this.foundWinLoss = true;
            }
        }else if (turn == State.Turn.BLACK) {
            if (winnerPlayer.equals(State.Turn.WHITEWIN))
                this.foundWinLoss = true;
            else if (winnerPlayer.equals(State.Turn.BLACKWIN)){
                this.foundWinLoss = true;
            }
        }
    }*/

    @Override
    public double eval(CustomState state, State.Turn player) {
        /*if(this.counter%100000==0){ // ogni 100000 evaluation sovrascrive la hashmap local con quella aggiornata
            this.game.getPlayers();
        }
        this.counter++;*/
        long start = System.currentTimeMillis();
        double eval = evalCapture(state, player);
        if(this.currDepthLimit == 1 && (eval==1.1 || eval == -1.1)){
            if(eval == 1.1)
                this.captureStates.add(state.toString());
        }
        super.eval(state, player);
        eval = this.game.getUtility(state, player);
        if(eval == Double.POSITIVE_INFINITY || eval == Double.NEGATIVE_INFINITY){
            this.foundWinLoss = true;
        }
        this.evalTime += System.currentTimeMillis() - start;
        return eval;
    }

    @Override
    public Action makeDecision(CustomState state) {
        long startTime = System.currentTimeMillis();
        this.foundWinLoss = false;
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
        if(!this.captureStates.isEmpty() && !this.foundWinLoss){
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

        //Non ho trovato catture o ho trovato vittorie/sconfitte e allora eseguo la migliore mossa secondo l'albero
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH) + " in " + getTimeInSeconds(startTime) +" seconds");
        return a;
    }

    @Override
    protected boolean hasSafeWinner(double resultUtility) {
        /*if(resultUtility == Double.NEGATIVE_INFINITY || resultUtility == Double.POSITIVE_INFINITY){
            this.foundWinLoss = true;
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
