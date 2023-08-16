package illustrator;

public interface Start {
    boolean shouldStart(float start, float time);

    interface Completable {
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

    final class After<T extends Completable> implements Start {
        public final T ref;
        public final float offset;

        private float countdown = -1f;

        public After(T ref) {
            this(ref, 0f);
        }

        public After(T ref, float offset) {
            this.ref = ref;
            this.offset = offset;
        }

        @Override
        public boolean shouldStart(float start, float time) {
            if(countdown == -1f) {
                if(ref.isCompleted()) {
                    countdown = time;
                } else {
                    return false;
                }
            }
            return time - countdown >= offset;
        }
    }
}
