package illustrator;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import illustrator.Start.*;
import illustrator.entity.*;

@SuppressWarnings("unchecked")
public class Entity implements Completable {
    public final Start start;
    public Transform globalTrns = new Transform(), localTrns = new Transform();
    public float z;
    public Color color = Color.white.cpy();

    private Entity parent;
    private final Seq<Entity> pendingChildren = new Seq<>(false), startedChildren = new Seq<>(false);
    private final Seq<Keyframe> pendingKeyframes = new Seq<>(false), startedKeyframes = new Seq<>(false);
    private final Seq<Runnable> enterListeners = new Seq<>(), exitListeners = new Seq<>();

    private boolean removed;
    float startTime;

    public Entity(Start start) {
        this.start = start;
    }

    public Entity child(Entity child) {
        child.parent = this;
        pendingChildren.add(child);
        return this;
    }

    public Entity color(Color color) {
        this.color.set(color);
        return this;
    }

    public <T extends Keyframe> T key(T keyframe) {
        keyframe.entity = this;
        pendingKeyframes.add(keyframe);
        return keyframe;
    }

    public Keyframe key(Start start, float duration, KeyframeListener listener) {
        var keyframe = new Keyframe(start, duration) {
            {
                entity = Entity.this;
            }

            @Override
            public void update(float lastTime) {
                listener.listen(this, lastTime);
            }
        };
        pendingKeyframes.add(keyframe);
        return keyframe;
    }

    public void update(float lastTime) {
        updateTrns();

        var pendingKeyframesIter = pendingKeyframes.iterator();
        while(pendingKeyframesIter.hasNext()) {
            var keyframe = pendingKeyframesIter.next();
            if(keyframe.start.shouldStart(startTime, Time.time)) {
                keyframe.startTime = Time.time;
                keyframe.onEnter();

                pendingKeyframesIter.remove();
                startedKeyframes.add(keyframe);
            }
        }

        var startedKeyframesIter = startedKeyframes.iterator();
        while(startedKeyframesIter.hasNext()) {
            var keyframe = startedKeyframesIter.next();
            keyframe.update(lastTime);

            if(keyframe.isCompleted()) {
                keyframe.onExit();
                startedKeyframesIter.remove();
            }
        }

        updateTrns();
        updateSelf(lastTime);

        var pendingChildrenIter = pendingChildren.iterator();
        while(pendingChildrenIter.hasNext()) {
            var child = pendingChildrenIter.next();
            if(child.start.shouldStart(startTime, Time.time)) {
                child.startTime = Time.time;
                child.onEnter();

                pendingChildrenIter.remove();
                startedChildren.add(child);
            }
        }

        var startedChildrenIter = startedChildren.iterator();
        while(startedChildrenIter.hasNext()) {
            var child = startedChildrenIter.next();
            child.update(lastTime);

            if(child.isRemoved()) {
                child.onExit();
                startedChildrenIter.remove();
            }
        }
    }

    public void updateSelf(float lastTime) {}

    public void draw(float lastTime) {
        drawSelf(lastTime);
        for(var child : startedChildren) child.draw(lastTime);
    }

    public void drawSelf(float lastTime) {}

    public void updateTrns() {
        if(parent == null) {
            globalTrns.set(localTrns);
        } else {
            globalTrns.set(parent.globalTrns).mul(localTrns);
        }
    }

    public void onEnter() {
        enterListeners.each(Runnable::run);
    }

    public void onExit() {
        exitListeners.each(Runnable::run);
    }

    protected void onEnter(Runnable run) {
        enterListeners.add(run);
    }

    protected void onExit(Runnable run) {
        exitListeners.add(run);
    }

    @Override
    public boolean isCompleted() {
        return
            pendingKeyframes.isEmpty() && startedKeyframes.isEmpty() &&
            pendingChildren.isEmpty() && !startedChildren.contains(e -> !e.isCompleted());
    }

    public float startTime() {
        return startTime;
    }

    public <T extends Entity> T parent() {
        return (T)parent;
    }

    public void remove() {
        removed = true;
        for(var child : startedChildren) {
            child.onExit();
            child.remove();
        }

        pendingChildren.clear();
        pendingKeyframes.clear();
    }

    public boolean isRemoved() {
        return removed;
    }

    public interface KeyframeListener {
        void listen(Keyframe keyframe, float lastTime);
    }
}
