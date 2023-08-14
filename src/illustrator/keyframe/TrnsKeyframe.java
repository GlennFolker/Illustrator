package illustrator.keyframe;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;

public class TrnsKeyframe extends Keyframe {
    public final Vec2 relTranslation;
    public final float relRotation;
    public final Vec2 relScale;
    public final Interp interpolation;

    public TrnsKeyframe(float start, float end, Vec2 relTranslation, float relRotation, Vec2 relScale, Interp interpolation) {
        super(start, end);
        this.relTranslation = relTranslation;
        this.relRotation = relRotation;
        this.relScale = relScale;
        this.interpolation = interpolation;
    }

    @Override
    public void update(float lastTime) {
        float time = Time.time;
        float now = interpolation.apply(time(time)), then = interpolation.apply(time(lastTime));

        entity.localTrns.translation.add(Tmp.v1
            .set(relTranslation).scl(now)
            .sub(Tmp.v2.set(relTranslation).scl(then))
        );
        entity.localTrns.rotation += relRotation * now - relRotation * then;
        entity.localTrns.scale.add(Tmp.v1
            .set(relScale).scl(now)
            .sub(Tmp.v2.set(relScale).scl(then))
        );
    }
}
