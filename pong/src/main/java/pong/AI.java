package pong;

import lombok.Data;
import network.Network;

import java.util.ArrayList;

@Data
public class AI implements Controller {
    final Paddle paddle;
    private Network network;
    boolean winner;

    OutputNode up;
    OutputNode down;

    public AI(Paddle paddle, Network network) {
        this.paddle = paddle;
        this.network = network;

        up = (OutputNode) network.getOutputNodes().get(0);
        down = (OutputNode) network.getOutputNodes().get(1);
    }

    @Override
    public void act(float deltaT, Ball ball) {
        boolean u = up.calculate() > Pong.fireOutput;
        boolean d = down.calculate() > Pong.fireOutput;
        network.resetMemory();

        if (u ^ d) {
            if (u) {
                paddle.up(deltaT);
            } else {
                paddle.down(deltaT);
            }
        }
    }

    @Override
    public void wins() {
        winner = true;
    }
}
