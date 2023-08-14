package illustrator.entity;

import arc.math.*;
import arc.math.geom.*;

public class Transform {
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
        translation.add(other.translation.x * Mathf.cos(rotation), other.translation.y * Mathf.sin(rotation));
        rotation += other.rotation;
        scale.scl(other.scale);
        return this;
    }
}
