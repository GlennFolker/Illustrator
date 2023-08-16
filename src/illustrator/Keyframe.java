package illustrator;

import arc.util.*;
import illustrator.Start.*;

public abstract class Keyframe implements Completable {
    public final Start start;
    public final float duration;

    public float startTime = -1f;
    public Entity entity;

    public Keyframe(Start start, float duration) {
        this.start = start;
        this.duration = duration;
    }

    public abstract void update(float lastTime);

    public void onEnter() {}
    public void onExit() {}

    @Override
    public boolean isCompleted() {
        return startTime != -1f && Time.time - startTime >= duration;
    }

    public float time() {
        return time(Time.time);
    }

    public float time(float time) {
        return (time - startTime) / duration;
    }
}
