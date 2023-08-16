package illustrator.entity;

import arc.math.geom.*;

public class Transform {
    private static final Vec2 tmp = new Vec2();

    public Vec2 translation = new Vec2();
    public float rotation;
    public Vec2 scale = new Vec2(1f, 1f);

    public Transform set(Transform other) {
        translation.set(other.translation);
        rotation = other.rotation;
        scale.set(other.scale);
        return this;
    }

    public Transform mul(Transform other) {
        translation.add(tmp.set(other.translation).scl(scale).rotate(rotation));
        rotation += other.rotation;
        scale.scl(other.scale);
        return this;
    }
}
