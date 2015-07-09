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
public class Paddle {
    public static int WIDTH = 20;
    public static int HEIGHT = 100;

    @NonFinal
    public static int speed;

    Vector2 pos;
    Rectangle rectangle;

    Sprite sprite;

    public Paddle(Vector2 pos, Color color) {
        this.pos = pos;
        rectangle = new Rectangle(pos.x, pos.y, WIDTH, HEIGHT);

        Pixmap pixmap = new Pixmap(WIDTH, HEIGHT, Pixmap.Format.RGB888);
        pixmap.setColor(color);
        pixmap.fill();
        sprite = new Sprite(new Texture(pixmap));
        sprite.setPosition(pos.x, pos.y);
    }

    public void up(float deltaT) {
        pos.add(0, -deltaT * speed);

        if (pos.y < 0) {
            pos.y = 0;
        }

        rectangle.setPosition(pos);
        sprite.setPosition(pos.x, pos.y);
    }

    public void down(float deltaT) {
        pos.add(0, deltaT * speed);

        if (pos.y > Field.height - HEIGHT) {
            pos.y = Field.height - HEIGHT;
        }

        rectangle.setPosition(pos);
        sprite.setPosition(pos.x, pos.y);
    }
}
