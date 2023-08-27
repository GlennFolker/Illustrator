package illustrator;

import arc.struct.*;
import arc.util.*;
import illustrator.Start.*;

public abstract class Keyframe implements Span {
    public final Start start;
    public final float duration;

    public float startTime = -1f;
    public Entity entity;

    private final Seq<Runnable> enterListeners = new Seq<>(), exitListeners = new Seq<>();

    public Keyframe(Start start, float duration) {
        this.start = start;
        this.duration = duration;
    }

    public abstract void update(float lastTime);

    public void onEnter() {
        enterListeners.each(Runnable::run);
    }

    public void onExit() {
        exitListeners.each(Runnable::run);
    }

    public void onEnter(Runnable run) {
        enterListeners.add(run);
    }

    public void onExit(Runnable run) {
        exitListeners.add(run);
    }

    @Override
    public boolean isStarted() {
        return startTime != -1f;
    }

    @Override
    public boolean isCompleted() {
        return isStarted() && Time.time - startTime >= duration;
    }

    public float time() {
        return time(Time.time);
    }

    public float time(float time) {
        return (time - startTime) / duration;
    }
}
