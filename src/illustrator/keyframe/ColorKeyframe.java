package illustrator.keyframe;

import arc.graphics.*;
import arc.math.*;

public class ColorKeyframe extends Keyframe {
    public final Color color;
    public final Interp interpolation;

    protected Color from;

    public ColorKeyframe(float start, float end) {
        this(start, end, Color.white);
    }

    public ColorKeyframe(float start, float end, Color color) {
        this(start, end, color, Interp.linear);
    }

    public ColorKeyframe(float start, float end, Color color, Interp interpolation) {
        super(start, end);
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
