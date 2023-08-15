package illustrator;

import arc.struct.*;
import arc.util.*;
import illustrator.entity.*;

public class Entities {
    private final Seq<Entity> pending = new Seq<>(false);
    private final Seq<Entity> drawing = new Seq<>(false);

    public Entities add(Entity entity) {
        pending.add(entity);
        for(var child : entity.children) add(child);
        return this;
    }

    public void update(float lastTime) {
        float time = Time.time;
        boolean dirty = false;

        var pendingIter = pending.iterator();
        while(pendingIter.hasNext()) {
            var e = pendingIter.next();
            if(time > e.start) {
                e.onEnter();

                pendingIter.remove();
                drawing.add(e);
                dirty = true;
            }
        }

        var drawingIter = drawing.iterator();
        while(drawingIter.hasNext()) {
            var e = drawingIter.next();
            if(e.isRemoved()) {
                e.onExit();
                drawingIter.remove();
                dirty = true;
            }
        }

        if(dirty) drawing.sort(e -> {
            int i = 0;
            for(var parent = e.parent; parent != null; parent = parent.parent) i += 1;
            return i;
        });

        for(var e : drawing) e.update(lastTime);
    }

    public boolean draw(float lastTime) {
        for(var e : drawing) e.draw(lastTime);
        return pending.isEmpty() && !drawing.contains(e -> !e.isDone());
    }
}
