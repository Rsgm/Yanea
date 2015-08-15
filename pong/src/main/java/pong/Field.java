package pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import lombok.Value;
import lombok.experimental.NonFinal;
import network.Network;

import java.util.ArrayList;

@Value
public class Field implements Screen {
    @NonFinal
    public static int width;
    @NonFinal
    public static int height;
    private final Follow p1;
    private final AI p2;
    private final ArrayList<InputNode> inputNodes;
    private final Pong pong;

    OrthographicCamera camera;
    SpriteBatch batch = new SpriteBatch();

    Ball ball;

    public Field(ArrayList<InputNode> inputNodes, Pong pong, Network network) {
        this.inputNodes = inputNodes;
        this.pong = pong;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width, height);

        Vector2 pos1 = new Vector2(0, height / 2 - Paddle.HEIGHT / 2);
        Vector2 pos2 = new Vector2(width - Paddle.WIDTH, height / 2 - Paddle.HEIGHT / 2);

        Paddle paddleL = new Paddle(pos1, Color.BLUE);
        Paddle paddleR = new Paddle(pos2, Color.RED);

        p1 = new Follow(paddleL);
        p2 = new AI(paddleR, network);

        ball = new Ball(p1, p2);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        delta = 0.016666668f;

        ball.update(delta, this);
        p1.act(delta, ball);
        p2.act(delta, null);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        ball.getSprite().draw(batch);
        p1.getPaddle().getSprite().draw(batch);
        p2.getPaddle().getSprite().draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        pong.nextRound();
    }
}
