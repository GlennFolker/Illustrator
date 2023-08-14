package illustrator.keyframe;

import arc.util.*;
import illustrator.entity.*;

public abstract class Keyframe {
    public Entity entity;
    public final float start, end;

    public Keyframe(float start, float end) {
        this.start = start;
        this.end = end;
    }

    public abstract void update(float lastTime);

    public void onEnter() {}
    public void onExit() {}

    public float time() {
        return time(Time.time);
    }

    public float time(float time) {
        time = Math.max(time, start + entity.start);
        return (time - start - entity.start) / (end - start);
    }

    public interface KeyframeListener {
        void listen(Entity entity, Keyframe keyframe, float lastTime);
    }
}
