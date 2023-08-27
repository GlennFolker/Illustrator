package illustrator;

import arc.func.*;

import static illustrator.Illustrator.*;

public interface Start {
    boolean shouldStart(float start, float time);

    interface Span {
        boolean isStarted();

        boolean isCompleted();
    }

    record Immediate() implements Start {
        @Override
        public boolean shouldStart(float start, float time) {
            return true;
        }
    }

    record Offset(float offset) implements Start {
        @Override
        public boolean shouldStart(float start, float time) {
            return time - start >= offset;
        }
    }

    abstract class Cond implements Start {
        public final float offset;
        private float countdown = -1f;

        public Cond(float offset) {
            this.offset = offset;
        }

        public abstract boolean conditionMet();

        @Override
        public boolean shouldStart(float start, float time) {
            if(countdown == -1f) {
                if(conditionMet()) {
                    countdown = time;
                } else {
                    return false;
                }
            }
            return time - countdown >= offset;
        }
    }

    final class Ref<T extends Span> extends Cond {
        public final T ref;
        public final Boolf<T> startup;

        public Ref(T ref, Boolf<T> startup) {
            this(ref, startup, 0f);
        }

        public Ref(T ref, Boolf<T> startup, float offset) {
            super(offset);
            this.ref = ref;
            this.startup = startup;
        }

        @Override
        public boolean conditionMet() {
            return startup.get(ref);
        }

        public static <T extends Span> Ref<T> when(T ref) {
            return when(ref, 0f);
        }

        public static <T extends Span> Ref<T> when(T ref, float offset) {
            return new Ref<>(ref, Span::isStarted, offset);
        }

        public static <T extends Span> Ref<T> after(T ref) {
            return after(ref, 0f);
        }

        public static <T extends Span> Ref<T> after(T ref, float offset) {
            return new Ref<>(ref, Span::isCompleted, offset);
        }
    }

    final class Click extends Cond {
        private boolean clicked;

        public Click() {
            this(0f);
        }

        public Click(float offset) {
            super(offset);
            inst.addClickListener(this);
        }

        public void click() {
            clicked = true;
        }

        @Override
        public boolean conditionMet() {
            return clicked;
        }
    }
}
