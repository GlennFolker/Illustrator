package illustrator.entity;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import illustrator.keyframe.*;
import illustrator.keyframe.Keyframe.*;

public abstract class Entity {
    public Transform localTrns = new Transform(), globalTrns = new Transform();

    public Color color = new Color();
    public float z;
    public float start, end;

    public Entity parent;
    public final Seq<Entity> children = new Seq<>();

    private final Seq<Keyframe> pendingFrames = new Seq<>(false);
    private final Seq<Keyframe> affectingFrames = new Seq<>(false);

    public Entity(float start, float end) {
        this.start = start;
        this.end = end;
    }

    public void child(Entity entity) {
        entity.parent = this;
        entity.start += start;
        entity.end += start;
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

    public abstract void draw(float lastTime);

    public void onEnter() {}
    public void onExit() {}

    public float time() {
        return time(Time.time);
    }

    public float time(float time) {
        return (time - start) / end;
    }
}
