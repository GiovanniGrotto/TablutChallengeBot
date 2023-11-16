package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class CustomState extends State {
    private final Game rules;
    //private Integer blackCaptures=0;
    //private Integer whitecaptures=0;


    public CustomState(Game rules, State state) {
        super();
        this.rules = rules;
        this.setBoard(state.getBoard());
        this.setTurn(state.getTurn());
    }

    public Game getRules(){
        return this.rules;
    }

    /*public Integer getBlackCaptures(){
        return this.blackCaptures;
    }

    public void setBlackCaptures(Integer blackCaptures) {
        this.blackCaptures = blackCaptures;
    }

    public Integer getWhitecaptures() {
        return whitecaptures;
    }

    public void setWhitecaptures(Integer whitecaptures) {
        this.whitecaptures = whitecaptures;
    }*/

    @Override
    public CustomState clone() {
        CustomState customStateClone = new CustomState(this.rules, this);

        Pawn[][] oldboard = this.getBoard();
        Pawn[][] newboard = new Pawn[9][9];

        for (int i = 0; i < this.board.length; i++) {
            System.arraycopy(oldboard[i], 0, newboard[i], 0, this.board[i].length);
        }
        customStateClone.setBoard(newboard);
        if(customStateClone.getWhitecaptures()>0 || customStateClone.getBlackCaptures()> 0){
            return customStateClone;
        }

        return customStateClone;
    }

}
