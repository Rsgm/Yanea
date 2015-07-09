package pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import lombok.Data;

@Data
public class Player implements Controller {
    final Paddle paddle;
    boolean winner;

    @Override
    public void act(float deltaT, Ball ball) {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            paddle.up(deltaT);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            paddle.down(deltaT);
        }
    }

    @Override
    public void wins() {
        winner = true;
    }
}
