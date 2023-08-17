package illustrator.keyframe;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import illustrator.*;

public class Translate extends Keyframe {
    public final Vec2 translation;
    public final Interp interpolation;
    public final boolean absolute;

    private Vec2 ref;

    public Translate(Start start, float duration, Vec2 translation, Interp interpolation, boolean absolute) {
        super(start, duration);
        this.translation = translation;
        this.interpolation = interpolation;
        this.absolute = absolute;
    }

    public static Translate to(Start start, float duration, Vec2 translation, Interp interpolation) {
        return new Translate(start, duration, translation, interpolation, true);
    }

    public static Translate by(Start start, float duration, Vec2 translation, Interp interpolation) {
        return new Translate(start, duration, translation, interpolation, false);
    }

    @Override
    public void onEnter() {
        if(absolute) ref = entity.localTrns.translation.cpy();
    }

    @Override
    public void update(float lastTime) {
        if(absolute) {
            entity.localTrns.translation.set(ref).lerp(translation, interpolation.apply(time()));
        } else {
            entity.localTrns.translation.add(Tmp.v1
                .set(translation).scl(interpolation.apply(time()))
                .sub(Tmp.v2.set(translation).scl(interpolation.apply(time(lastTime))))
            );
        }
    }
}
