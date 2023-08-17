package illustrator.keyframe;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import illustrator.*;

public class Scale extends Keyframe {
    public final Vec2 scale;
    public final Interp interpolation;
    public final boolean absolute;

    private Vec2 ref;

    public Scale(Start start, float duration, Vec2 scale, Interp interpolation, boolean absolute) {
        super(start, duration);
        this.scale = scale;
        this.interpolation = interpolation;
        this.absolute = absolute;
    }

    public static Scale to(Start start, float duration, Vec2 scale, Interp interpolation) {
        return new Scale(start, duration, scale, interpolation, true);
    }

    public static Scale by(Start start, float duration, Vec2 scale, Interp interpolation) {
        return new Scale(start, duration, scale, interpolation, false);
    }

    @Override
    public void onEnter() {
        if(absolute) ref = entity.localTrns.scale.cpy();
    }

    @Override
    public void update(float lastTime) {
        if(absolute) {
            entity.localTrns.scale.set(ref).lerp(scale, interpolation.apply(time()));
        } else {
            entity.localTrns.scale.add(Tmp.v1
                .set(scale).scl(interpolation.apply(time()))
                .sub(Tmp.v2.set(scale).scl(interpolation.apply(time(lastTime))))
            );
        }
    }
}
