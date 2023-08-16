package illustrator.keyframe;

import arc.graphics.*;
import arc.math.*;
import illustrator.*;

public class ColorKeyframe extends Keyframe {
    public final Color color;
    public final Interp interpolation;

    protected Color from;

    public ColorKeyframe(Start start, float duration) {
        this(start, duration, Color.white);
    }

    public ColorKeyframe(Start start, float duration, Color color) {
        this(start, duration, color, Interp.linear);
    }

    public ColorKeyframe(Start start, float duration, Color color, Interp interpolation) {
        super(start, duration);
        this.color = color;
        this.interpolation = interpolation;
    }

    @Override
    public void onEnter() {
        from = entity.color.cpy();
    }

    @Override
    public void update(float lastTime) {
        entity.color.set(from).lerp(color, interpolation.apply(time()));
    }
}
