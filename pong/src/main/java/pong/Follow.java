package pong;

import lombok.Data;

@Data
public class Follow implements Controller {
    final Paddle paddle;
    boolean winner;

    @Override
    public void act(float deltaT, Ball ball) {
        if (ball.getPos().y < paddle.getPos().y + Paddle.HEIGHT / 2 - ball.getRectangle().height / 2) {
            paddle.up(deltaT);
        } else {
            paddle.down(deltaT);
        }
    }

    @Override
    public void wins() {
        winner = true;
    }
}
