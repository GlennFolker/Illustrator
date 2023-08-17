package illustrator.keyframe;

import arc.math.*;
import arc.util.*;
import illustrator.*;

public class Rotate extends Keyframe {
    public final float rotation;
    public final Interp interpolation;
    public final boolean absolute;

    private float ref;

    public Rotate(Start start, float duration, float rotation, Interp interpolation, boolean absolute) {
        super(start, duration);
        this.rotation = rotation;
        this.interpolation = interpolation;
        this.absolute = absolute;
    }

    public static Rotate to(Start start, float duration, float rotation, Interp interpolation) {
        return new Rotate(start, duration, rotation, interpolation, true);
    }

    public static Rotate by(Start start, float duration, float rotation, Interp interpolation) {
        return new Rotate(start, duration, rotation, interpolation, false);
    }

    @Override
    public void onEnter() {
        if(absolute) ref = entity.localTrns.rotation;
    }

    @Override
    public void update(float lastTime) {
        entity.localTrns.rotation = absolute
            ? Mathf.lerp(ref, rotation, interpolation.apply(time()))
            : (entity.localTrns.rotation + rotation * interpolation.apply(time()) - rotation * interpolation.apply(time(lastTime)));
    }
}
