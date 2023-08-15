package illustrator.entity;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import illustrator.keyframe.*;
import illustrator.keyframe.Keyframe.*;

public abstract class Entity {
    public Transform localTrns = new Transform(), globalTrns = new Transform();

    public Color color = new Color();
    public float z;
    public float start;

    public Entity parent;
    public final Seq<Entity> children = new Seq<>();

    private final Seq<Keyframe> pendingFrames = new Seq<>(false), affectingFrames = new Seq<>(false);
    private final Seq<Runnable> onEnter = new Seq<>(), onExit = new Seq<>();

    private boolean removed;

    public Entity(float start) {
        this.start = start;
    }

    public void child(Entity entity) {
        entity.parent = this;
        entity.start += start;
        children.add(entity);
    }

    public void key(Keyframe keyframe) {
        keyframe.entity = this;
        pendingFrames.add(keyframe);
    }

    public void key(float start, float end, KeyframeListener listener) {
        pendingFrames.add(new Keyframe(start, end) {
            {
                entity = Entity.this;
            }

            @Override
            public void update(float lastTime) {
                listener.listen(Entity.this, this, lastTime);
            }
        });
    }

    public void update(float lastTime) {
        float time = Time.time;

        if(parent != null) {
            globalTrns.set(parent.globalTrns).mul(localTrns);
        } else {
            globalTrns.set(localTrns);
        }

        var pendingIter = pendingFrames.iterator();
        while(pendingIter.hasNext()) {
            var key = pendingIter.next();
            if(time > start + key.start) {
                key.onEnter();

                pendingIter.remove();
                affectingFrames.add(key);
            }
        }

        var affectingIter = affectingFrames.iterator();
        while(affectingIter.hasNext()) {
            var key = affectingIter.next();
            key.update(lastTime);

            if(time >= start + key.end) {
                key.onExit();
                affectingIter.remove();
            }
        }
    }

    public boolean isDone() {
        return pendingFrames.isEmpty() && affectingFrames.isEmpty();
    }

    public abstract void draw(float lastTime);

    public void entered() {
        for(var run : onEnter) run.run();
    }

    public void exited() {
        for(var run : onExit) run.run();
    }
    
    public void onEnter(Runnable run) {
        onEnter.add(run);
    }

    public void onExit(Runnable run) {
        onExit.add(run);
    }

    public void remove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }
}
