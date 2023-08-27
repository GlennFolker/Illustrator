package illustrator.modules.ecs;

import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import illustrator.*;
import illustrator.Start.*;
import illustrator.entity.*;
import illustrator.graphics.*;
import illustrator.keyframe.*;
import illustrator.modules.ecs.entity.*;

import static arc.Core.*;
import static illustrator.Illustrator.*;

public class ECS implements IllustratorModule {
    private Font titleFont, subtitleFont;

    @Override
    public void init() {
        Lines.setCirclePrecision(8f);
        titleFont = inst.fonts.bold.generateFont(new FreeTypeFontParameter() {{ size = 192; }});
        subtitleFont = inst.fonts.semibold.generateFont(new FreeTypeFontParameter() {{ size = 96; }});

        class Intro {
            static Keyframe
                lineAppear, lineDisappear, circleDisappear,
                dragTop, subtitleTyping;
            static Text title;
            static Vec2 dragTopValue;
        }

        Entity blow;
        inst.root.color(Palettes.black)
            // Intro explosion.
            .child(blow = new Entity(new Click()) {
                float in;

                {
                    z = -100f;

                    var pop = key(new Immediate(), 30f, (keyframe, lastTime) -> in = keyframe.time());
                    key(Ref.after(pop), 0f, (keyframe, lastTime) -> {
                        remove();
                        inst.root.color(Palettes.white);
                    });
                }

                @Override
                public void drawSelf(float lastTime) {
                    float rad = Mathf.len(camera.width / 2f, camera.height / 2f);

                    Draw.z(z);
                    Draw.color(Palettes.darkGray);
                    Draws.fCircle(camera.position, Interp.pow5Out.apply(in) * rad);

                    Draw.color(Palettes.lightGray);
                    Draws.fCircle(camera.position, Interp.pow4Out.apply(in) * rad);

                    Draw.color(Palettes.white);
                    Draws.fCircle(camera.position, Interp.pow3Out.apply(in) * rad);
                }
            })
            // Intro title.
            .child(new Text(Ref.when(blow), titleFont) {{
                Intro.title = this;

                localTrns.translation.set(0f, 0f);
                localTrns.scale.set(0f, 0f);
                color.set(Palettes.lightestGreen);

                text = "ECS";
                float radius = 240f;

                // Pop out.
                var initZoomIn = key(Scale.to(new Immediate(), 8f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(Scale.to(Ref.after(initZoomIn), 8f, new Vec2(1f, 1f), Interp.pow2));

                // Surrounding circle.
                child(new Entity(Ref.after(initZoomIn)) {
                    float circle;
                    float line;

                    @Override
                    public void drawSelf(float lastTime) {
                        Draw.z(z);
                        Lines.stroke(30f, color);
                        Lines.arc(
                            globalTrns.translation.x, globalTrns.translation.y,
                            radius, circle * 348f / 360f, 12f + globalTrns.rotation,
                            Lines.circleVertices(radius) * 3
                        );

                        Tmp.v1.trns(globalTrns.rotation, radius).add(globalTrns.translation);
                        Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, globalTrns.rotation, 120f * line);
                    }

                    {
                        color.set(Palettes.lightestGreen);

                        // Rotate to appear.
                        float cAppear = 45f, lAppear = 15f;
                        key(Rotate.by(new Immediate(), cAppear + lAppear, 45f, Interp.pow3));

                        // Appear.
                        var cAppearKey = key(new Immediate(), cAppear, (keyframe, lastTime) -> circle = Interp.pow2In.apply(keyframe.time()));
                        Intro.lineAppear = key(Ref.after(cAppearKey), lAppear, (keyframe, lastTime) -> line = Interp.pow3Out.apply(keyframe.time()));

                        // Disappear.
                        Intro.lineDisappear = key(Ref.after(Intro.lineAppear, 20f), lAppear, (keyframe, lastTime) -> line = 1f - Interp.pow3In.apply(keyframe.time()));
                        Intro.circleDisappear = key(Ref.after(Intro.lineDisappear), cAppear, (keyframe, lastTime) -> circle = 1f - Interp.pow4.apply(keyframe.time()));

                        // Rotate to disappear.
                        key(Rotate.by(Ref.after(Intro.lineAppear, 6f), 40f, -90f, Interp.smooth2));
                        key(Rotate.by(Ref.after(Intro.lineDisappear), Intro.circleDisappear.duration, 60f, Interp.smooth2));
                    }
                });

                // Drag to top-left based on radius.
                key(Translate.by(Ref.after(Intro.lineAppear, 20f), 30f, new Vec2(
                    //-videoWidth / 2f + radius + 48f,
                    (camera.position.x - camera.width / 2f) + radius + 48f,
                    (camera.position.y + camera.height / 2f) - radius - 48f
                ), Interp.smooth2));

                // Drag to top-left based on layout size.
                Intro.dragTop = key(new Wrap(Ref.after(Intro.circleDisappear), 30f, (s, d) -> Translate.by(s, d, Intro.dragTopValue = new Vec2(
                    0f,
                    (-videoWidth / 2f - globalTrns.translation.x + width / 2f) + videoHeight / 2f - globalTrns.translation.y - height / 2f
                ), Interp.pow3)));
            }})
            // Intro subtitle.
            .child(new Text(Ref.after(Intro.lineDisappear, 25f), subtitleFont) {{
                color.set(Palettes.lighterGreen);

                halign = Align.left;
                valign = Align.top;

                onEnter(() -> localTrns.translation
                    .set(Intro.title.globalTrns.translation)
                    .sub(Intro.title.width / 2f, Intro.title.height / 2f + 24f)
                );

                // Type the subtitle out for 3/4 seconds.
                Intro.subtitleTyping = key(new Typing(new Immediate(), 45f, "", "Entity Component System"));
                // Follow the title, in a bit of a delay.
                key(new Wrap(Ref.when(Intro.dragTop, 2f), Intro.dragTop.duration, (s, d) -> Translate.by(s, d, Intro.dragTopValue, Interp.pow3)));
            }})
            // Entity.
            .child(new Ent(Ref.after(Intro.subtitleTyping, 20f)) {{
                color.set(Palettes.darkerGray);
                localTrns.translation.set(camera.position).sub(camera.width / 2f - 96f, camera.height / 2f - 96f);

                align = Align.bottomLeft;
                height = 480f;

                key(new Immediate(), 12f, (keyframe, lastTime) -> torso = Interp.pow3.apply(keyframe.time()));
                key(new Offset(4f), 16f, (keyframe, lastTime) -> head = Interp.pow3.apply(keyframe.time()));
                key(new Offset(12f), 20f, (keyframe, lastTime) -> hands = Interp.pow3Out.apply(keyframe.time()));
                key(new Offset(16f), 20f, (keyframe, lastTime) -> legs = Interp.pow3Out.apply(keyframe.time()));
            }});
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        subtitleFont.dispose();
    }
}
