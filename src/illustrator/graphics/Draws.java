package illustrator.graphics;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;

public final class Draws {
    private static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();

    private Draws() {
        throw new AssertionError();
    }

    public static void fCircle(Position pos, float radius) {
        fCircle(pos.getX(), pos.getY(), radius);
    }

    public static void fCircle(float x, float y, float radius) {
        fCircle(x, y, radius, 0f, 1f);
    }

    public static void fCircle(Position pos, float radius, float rotation, float fraction) {
        fCircle(pos.getX(), pos.getY(), radius, rotation, fraction);
    }

    public static void fCircle(float x, float y, float radius, float rotation, float fraction) {
        int vertices = Mathf.round(Lines.circleVertices(radius), 2);

        float coverage = 360f * fraction;
        for(int i = 0; i < vertices; i += 2) {
            v1.trns(rotation + (float)i / vertices * coverage, radius).add(x, y);
            v2.trns(rotation + (i + 1f) / vertices * coverage, radius).add(x, y);
            v3.trns(rotation + (i + 2f) / vertices * coverage, radius).add(x, y);
            Fill.quad(x, y, v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
        }
    }

    public static void roundRect(Position pos, float width, float height, float rotation, float radius) {
        roundRect(pos.getX(), pos.getY(), width, height, rotation, radius);
    }

    public static void roundRect(float x, float y, float width, float height, float rotation, float radius) {
        for(int i = 0; i < 4; i++) {
            int left = switch(i) {
                case 0, 3 -> 1;
                case 1, 2 -> -1;
                default -> throw new AssertionError();
            };
            int bottom = switch(i) {
                case 0, 1 -> 1;
                case 2, 3 -> -1;
                default -> throw new AssertionError();
            };

            v1.set(left * (width / 2f - radius), bottom * (height / 2f - radius)).rotate(rotation).add(x, y);
            fCircle(v1.x, v1.y, radius, rotation + i * 90f, 0.25f);
        }

        for(int sign : Mathf.signs) {
            v1.set(0f, height * Mathf.num(sign == 1) - radius / 2f * sign - height / 2f).rotate(rotation).add(x, y);
            Fill.rect(v1.x, v1.y, width - radius * 2f, radius, rotation);
        }

        for(int sign : Mathf.signs) {
            v1.set(width * Mathf.num(sign == 1) - radius / 2f * sign - width / 2f, 0f).rotate(rotation).add(x, y);
            Fill.rect(v1.x, v1.y, radius, height - radius * 2f, rotation);
        }

        Fill.rect(x, y, width - radius * 2f, height - radius * 2f, rotation);
    }
}
