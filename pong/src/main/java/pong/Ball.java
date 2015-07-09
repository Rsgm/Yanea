package pong;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class Ball {
    @NonFinal
    public static float speed; // px/sec
    Rectangle rectangle = new Rectangle(0, 0, 30, 30);

    Vector2 pos; // todo measure fitness by x distance, not time
    Vector2 velocity;

    Paddle paddleL;
    Paddle paddleR;

    Controller p1;
    Controller p2;

    Sprite sprite;

    @NonFinal
    int hits;

    public Ball(Controller p1, Controller p2) {
        this.p1 = p1;
        this.p2 = p2;

        this.paddleL = p1.getPaddle();
        this.paddleR = p2.getPaddle();

        pos = new Vector2(Field.width / 3, Field.height / 2);
        float direction = (float) ((Math.random() * 15 + 20));
        velocity = new Vector2(speed, 0).rotate(Math.random() > 0.5f ? direction : -direction);

        Pixmap pixmap = new Pixmap(30, 30, Pixmap.Format.RGB888);
        pixmap.setColor(Color.GREEN);
        pixmap.fill();
        sprite = new Sprite(new Texture(pixmap));
        sprite.setPosition(pos.x, pos.y);
    }

    public void update(float deltaT, Field field) {
        Vector2 next = pos.cpy().add(velocity.cpy().scl(deltaT));
        Rectangle nextRectangle = new Rectangle(next.x, next.y, 30, 30);

        if (next.y < 0) {
            Vector2 mov = next.cpy().sub(pos);

            float angle = mov.angleRad(Vector2.X);

            Vector2 touch = new Vector2((float) (pos.y / Math.tan(angle)), pos.y);

            pos.set(pos.x, -mov.cpy().sub(touch).y);
            velocity.rotateRad(angle * 2);
        } else if (next.y + 30 > Field.height) {
            Vector2 mov = next.cpy().sub(pos);

            float angle = mov.angleRad(Vector2.X);

            Vector2 touch = new Vector2((float) ((Field.height - pos.y + 30) / Math.tan(angle)), Field.height - pos.y - 30);

            pos.set(pos.x, Field.height - mov.cpy().sub(touch).y - 30);
            velocity.rotateRad(angle * 2);
        } else if (paddleL.getRectangle().overlaps(nextRectangle)) {
            Vector2 mov = next.cpy().sub(pos);

            float angle = mov.angleRad(Vector2.Y);

            Vector2 touch = new Vector2(pos.x - Paddle.WIDTH, (float) (Math.tan(angle) / pos.x));

            pos.set(Paddle.WIDTH - mov.cpy().sub(touch).x, pos.y);
            velocity.rotateRad(angle * 2);
            velocity.rotate((float) (Math.random() * 40 - 10));
            velocity.scl((float) (Math.random() * .1 + 1));
        } else if (paddleR.getRectangle().overlaps(nextRectangle)) {
            hits++; // p2 hit the paddle
            Vector2 mov = next.cpy().sub(pos);

            float angle = mov.angleRad(Vector2.Y);

            Vector2 touch = new Vector2(Field.width - pos.x - Paddle.WIDTH, (float) (Math.tan(angle) / Field.width - pos.x - Paddle.WIDTH));

            pos.set(Field.width - Paddle.WIDTH - mov.cpy().sub(touch).x - 60, pos.y);
            velocity.rotateRad(angle * 2);
            velocity.rotate((float) (Math.random() * 40 - 10));
            velocity.scl((float) (Math.random() * .1 + 1));
        } else if (pos.x < 0) {
            p2.wins();
            field.dispose();
        } else if (pos.x > Field.width - 30) {
            p1.wins();
            field.dispose();
        } else {
            pos.set(next);
        }

        float angle = velocity.angle(Vector2.Y);
        if ((angle > -20 && angle <= 0) || angle > 160) {
            velocity.rotate((float) -(Math.random() * 20 + 20));
        } else if ((angle < 20 && angle >= 0) || angle < -160) {
            velocity.rotate((float) (Math.random() * 20 - 20));
        }

        this.rectangle.setPosition(pos);
        sprite.setPosition(pos.x, pos.y);
    }
}
