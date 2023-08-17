package illustrator;

import arc.func.*;

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

    final class Ref<T extends Span> implements Start {
        public final T ref;
        public final Boolf<T> startup;
        public final float offset;

        private float countdown = -1f;

        public Ref(T ref, Boolf<T> startup) {
            this(ref, startup, 0f);
        }

        public Ref(T ref, Boolf<T> startup, float offset) {
            this.ref = ref;
            this.startup = startup;
            this.offset = offset;
        }

        @Override
        public boolean shouldStart(float start, float time) {
            if(countdown == -1f) {
                if(startup.get(ref)) {
                    countdown = time;
                } else {
                    return false;
                }
            }
            return time - countdown >= offset;
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
}
