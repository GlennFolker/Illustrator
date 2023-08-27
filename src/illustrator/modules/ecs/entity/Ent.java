package illustrator.modules.ecs.entity;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import illustrator.*;
import illustrator.graphics.*;

public class Ent extends Entity {
    public int align = Align.center;
    public float width, height;
    public float head, torso, hands, legs;

    public Ent(Start start) {
        super(start);
    }

    @Override
    public void updateTrns() {
        super.updateTrns();
        width = height * 2f / 3f;
    }

    @Override
    public void drawSelf(float lastTime) {
        Draw.z(z);
        Lines.stroke(width / 7.5f, color);

        var origin = Tmp.v1.set(
            Align.isLeft(align) ? -width : Align.isRight(align) ? width : 0f,
            Align.isBottom(align) ? -height : Align.isTop(align) ? height : 0f
        ).scl(0.5f);

        float rot = globalTrns.rotation;
        var trns = globalTrns.translation;

        Tmp.v2.set(0f, height / 2f - 0.09f * height).sub(origin).rotate(rot).add(trns);
        Draws.fCircle(Tmp.v2, height * 0.09f * head);

        if(width * torso >= width / 5f) {
            Tmp.v2.set(0f, height / 2f - 0.4f * height).sub(origin).rotate(rot).add(trns);
            Draws.roundRect(Tmp.v2, width / 2.5f * torso, height * 0.4f * torso, rot, width / 20f);
        }

        if(hands > 0f) {
            for(int sign : Mathf.signs) {
                Tmp.v2.set((width / 5f - width / 17f) * sign, height / 2f - 0.2f * height - width / 15f).sub(origin).rotate(rot).add(trns);
                Tmp.v3
                    .set(Tmp.v2).lerp(
                        Tmp.v4
                            .set((width / 2f - width / 15f) * sign, -0.05f * height)
                            .sub(origin).rotate(rot).add(trns),
                        hands
                    );

                float angle = Tmp.v2.angleTo(Tmp.v3);
                Draws.fCircle(Tmp.v2, width / 15f, angle + 90f, 0.5f);
                Lines.line(Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, false);
                Draws.fCircle(Tmp.v3, width / 15f, angle - 90f, 0.5f);
            }
        }

        if(legs > 0f) {
            for(int sign : Mathf.signs) {
                Tmp.v2.set((width / 5f - width / 15f) * sign, height / 2f - 0.55f * height).sub(origin).rotate(rot).add(trns);
                Tmp.v3
                    .set(Tmp.v2).lerp(
                        Tmp.v4
                            .set((width / 5f - width / 15f) * sign, height / 2f - height + height / 15f)
                            .sub(origin).rotate(rot).add(trns),
                        legs
                    );

                float angle = Tmp.v2.angleTo(Tmp.v3);
                Draws.fCircle(Tmp.v2, width / 15f, angle + 90f, 0.5f);
                Lines.line(Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, false);
                Draws.fCircle(Tmp.v3, width / 15f, angle - 90f, 0.5f);
            }
        }
    }
}
