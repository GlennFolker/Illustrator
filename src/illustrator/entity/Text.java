package illustrator.entity;

import arc.graphics.g2d.*;
import arc.util.*;
import illustrator.*;

public class Text extends Entity {
    public Font font;
    public String text = "";
    public int halign = Align.center, valign = Align.center;
    public float targetWidth = 0f;
    public boolean wrap = false;

    public float width, height;

    public Text(Start start, Font font) {
        super(start);
        this.font = font;
    }

    @Override
    public void updateSelf(float lastTime) {
        var layout = GlyphLayout.obtain();
        layout.setText(font, text, color, targetWidth, halign, wrap);

        width = layout.width;
        height = layout.height;
        layout.free();
    }

    @Override
    public void drawSelf(float lastTime) {
        font.setColor(color);
        font.setUseIntegerPositions(true);

        Draw.z(z);
        font.getData().setScale(
            Math.max(globalTrns.scale.x, 0.00001f),
            Math.max(globalTrns.scale.y, 0.00001f)
        );

        var layout = GlyphLayout.obtain();
        layout.setText(font, text, color, targetWidth, halign, wrap);
        font.draw(text, globalTrns.translation.x, globalTrns.translation.y + (
            (valign & Align.bottom) != 0 ? layout.height :
            (valign & Align.center) != 0 ? layout.height / 2f :
            0f
        ), targetWidth, halign, wrap);
        layout.free();

        font.getData().setScale(1f);
    }

    public class TypingKeyframe extends Keyframe {
        public String initial;
        public final String text;

        public TypingKeyframe(Start start, float duration, String initial, String text) {
            super(start, duration);
            this.initial = initial;
            this.text = text;
        }

        @Override
        public void onEnter() {
            if(initial == null) initial = Text.this.text;
        }

        @Override
        public void update(float lastTime) {
            Text.this.text = (initial + text).substring(initial.length(), initial.length() + (int)(time() * text.length()));
        }
    }
}
