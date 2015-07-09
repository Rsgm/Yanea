package pong;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public interface Controller {
    void act(float deltaT, Ball ball);

    Paddle getPaddle();

    void wins();
}
