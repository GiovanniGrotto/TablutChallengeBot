package it.unibo.ai.didattica.competition.tablut.customizations;

import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class CustomState extends State {
    private final Game rules;

    public CustomState(Game rules) {
        super();
        this.rules = rules;
    }

    public Game getRules(){
        return this.rules;
    }
}
