package pong;

import lombok.Data;

import java.util.ArrayList;

@Data
public class AI implements Controller {
    final Paddle paddle;
    boolean winner;

    OutputNode up;
    OutputNode down;

    public AI(Paddle paddle, ArrayList<OutputNode> outputNodes) {
        this.paddle = paddle;

        up = outputNodes.get(0);
        down = outputNodes.get(1);
    }

    @Override
    public void act(float deltaT, Ball ball) {
        double u = up.calculate();
        double d = down.calculate();

        if (u == 1 ^ d == 1) {
            if (u == 1) {
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
