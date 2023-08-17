package illustrator.keyframe;

import illustrator.*;

public class Wrap extends Keyframe {
    public final KeyframeProvider provider;
    protected Keyframe inner;

    public Wrap(Start start, float duration, KeyframeProvider provider) {
        super(start, duration);
        this.provider = provider;
    }

    @Override
    public void onEnter() {
        inner = provider.get(start, duration);
        inner.startTime = startTime;
        inner.entity = entity;
        inner.onEnter();
    }

    @Override
    public void onExit() {
        inner.onExit();
    }

    @Override
    public void update(float lastTime) {
        inner.update(lastTime);
    }

    public interface KeyframeProvider {
        Keyframe get(Start start, float duration);
    }
}
