package illustrator.entity;

import arc.graphics.g2d.*;

import static arc.Core.*;

public class Background extends Entity {
    public Background(float start, float end) {
        super(start, end);
    }

    @Override
    public void draw(float lastTime) {
        Draw.z(Float.NEGATIVE_INFINITY);

        Draw.color(color);
        Fill.rect(camera.position.x, camera.position.y, camera.width, camera.height);
        Draw.color();
    }
}
