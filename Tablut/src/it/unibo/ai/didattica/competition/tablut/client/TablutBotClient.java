package it.unibo.ai.didattica.competition.tablut.client;

import it.unibo.ai.didattica.competition.tablut.customizations.AlphaBetaPlayer;
import it.unibo.ai.didattica.competition.tablut.customizations.CustomState;
import it.unibo.ai.didattica.competition.tablut.customizations.GameModel;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.IOException;
import java.net.UnknownHostException;

public class TablutBotClient extends TablutClient{

    public AlphaBetaPlayer player;
    int timeout;

    public TablutBotClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
        this.timeout = timeout;
    }

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
        String role = "";
        String name = "sigmaZero";
        String ipAddress = "localhost";
        int timeout = 60;

        if (args.length < 1) {
            System.out.println("You must specify which player you are (WHITE or BLACK)");
            System.exit(-1);
        } else {
            System.out.println(args[0]);
            role = (args[0]);
        }
        if (args.length == 2) {
            System.out.println(args[1]);
            timeout = Integer.parseInt(args[1]);

        }
        if (args.length == 3) {
            ipAddress = args[2];
        }
        System.out.println("Selected client: " + args[0]);

        TablutBotClient client = new TablutBotClient(role, name, timeout, ipAddress);
        client.run();
    }
    @Override
    public void run() {
        this.player = new AlphaBetaPlayer(new GameModel(),0, 2, this.timeout-2);

        try {
            this.declareName();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        System.out.println("Running...");

        while (true){
            try {
                this.read();
            } catch (ClassNotFoundException | IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
            //System.out.println("Current state: ");
            CustomState currentState = this.getCustomCurrentState();
            State.Turn turn = currentState.getTurn();
            State.Turn player = this.getPlayer();

            if(turn.equals(player)) {
                Action bestAction = this.player.makeDecision(currentState);
                try {
                    this.write(bestAction);
                } catch (ClassNotFoundException | IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public CustomState getCustomCurrentState() {
        State state = getCurrentState();
        return new CustomState(new GameAshtonTablut(0, 0, "garbage", "fake", "fake"), state);
    }

}
