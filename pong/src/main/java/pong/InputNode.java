package pong;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import network.nodes.Node;

@Value
@RequiredArgsConstructor
public class InputNode implements Node {
    @NonFinal
    @Setter
    Ball ball;
    @NonFinal
    @Setter
    Follow p1;
    @NonFinal
    @Setter
    AI p2;

    Input input;

    @Override
    public double calculate() {
        switch (input) {
            case BALL_X:
                return ball.getPos().x / ball.getPos().len();
            case BALL_Y:
                return ball.getPos().y / ball.getPos().len();
            case PADLE_L:
                return p1.getPaddle().getPos().y / Field.height;
            case PADDLE_R:
                return p2.getPaddle().getPos().y / Field.height;
            case VELOCITY_X:
                return (ball.getVelocity().x / ball.getVelocity().len()) / 2d + 0.5d;
            case VELOCITY_Y:
                return (ball.getVelocity().y / ball.getVelocity().len()) / 2d + 0.5d;
        }

        return 0;
    }

    @Override
    @Deprecated
    /**
     * Input nodes may not have inputs themselves.
     */
    public void connect(Node input, double weight) {
        // do nothing
    }
}
