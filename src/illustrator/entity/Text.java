package illustrator.entity;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;

public class Text extends Entity {
    public Font font;
    public String text = "";
    public int align = Align.center;
    public float targetWidth = 0f;
    public boolean wrap = false;

    public Text(float start, Font font) {
        super(start);
        this.font = font;
    }

    @Override
    public void draw(float lastTime) {
        font.setColor(color);
        font.setUseIntegerPositions(false);

        Draw.z(z);
        font.getData().setScale(
            Math.max(globalTrns.scale.x, 0.00001f),
            Math.max(globalTrns.scale.y, 0.00001f)
        );

        var layout = GlyphLayout.obtain();
        layout.setText(font, text, color, targetWidth, align, wrap);
        font.draw(text, globalTrns.translation.x, globalTrns.translation.y + layout.height / 2f, targetWidth, align, wrap);
        layout.free();

        font.getData().setScale(1f);
    }
}
