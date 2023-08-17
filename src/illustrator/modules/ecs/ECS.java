package illustrator.modules.ecs;

import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import illustrator.*;
import illustrator.Start.*;
import illustrator.entity.*;
import illustrator.keyframe.*;

import static illustrator.Illustrator.*;

public class ECS implements IllustratorModule {
    private Font titleFont, subtitleFont;

    @Override
    public void init(Illustrator illustrator) {
        titleFont = illustrator.fonts.bold.generateFont(new FreeTypeFontParameter() {{ size = 192; }});
        subtitleFont = illustrator.fonts.semibold.generateFont(new FreeTypeFontParameter() {{ size = 96; }});

        class Intro {
            static Keyframe
                lineAppear, lineDisappear, circleDisappear,
                dragTop;
            static Text title;
            static Vec2 dragTopValue;
        }

        illustrator.root
            .color(Color.white)
            .child(new Text(new Immediate(), titleFont) {{
                Intro.title = this;

                localTrns.translation.set(0f, 0f);
                localTrns.scale.set(0f, 0f);
                color.set(Palettes.green);

                text = "ECS";
                float radius = 240f;

                // Pop out.
                var initZoomIn = key(Scale.to(new Immediate(), 8f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(Scale.to(Ref.after(initZoomIn), 8f, new Vec2(1f, 1f), Interp.pow2));

                child(new Entity(Ref.after(initZoomIn)) {
                    float circle;
                    float line;

                    @Override
                    public void draw(float lastTime) {
                        Lines.stroke(30f, color);
                        Lines.arc(
                            globalTrns.translation.x, globalTrns.translation.y,
                            radius, circle * 348f / 360f, 12f + globalTrns.rotation,
                            Lines.circleVertices(radius)
                        );

                        Tmp.v1.trns(globalTrns.rotation, radius).add(globalTrns.translation);
                        Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, globalTrns.rotation, 120f * line);
                    }

                    {
                        color.set(Palettes.green);

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
                    -videoWidth / 2f + radius + 48f,
                    videoHeight / 2f - radius - 48f
                ), Interp.smooth2));

                // Drag to top-left based on layout size.
                Intro.dragTop = key(new Wrap(Ref.after(Intro.circleDisappear), 30f, (s, d) -> Translate.by(s, d, Intro.dragTopValue = new Vec2(
                    0f,
                    (-videoWidth / 2f - globalTrns.translation.x + width / 2f) + videoHeight / 2f - globalTrns.translation.y - height / 2f
                ), Interp.pow3)));
            }})
            .child(new Text(Ref.after(Intro.lineDisappear, 25f), subtitleFont) {{
                color.set(Palettes.darkGreen);

                halign = Align.left;
                valign = Align.top;

                onEnter(() -> localTrns.translation
                    .set(Intro.title.globalTrns.translation)
                    .sub(Intro.title.width / 2f, Intro.title.height / 2f + 24f)
                );

                // Type the subtitle out for 3/4 seconds.
                key(new TypingKeyframe(new Immediate(), 45f, "", "Entity Component System"));
                // Follow the title, in a bit of a delay.
                key(new Wrap(Ref.when(Intro.dragTop, 5f), Intro.dragTop.duration, (s, d) -> Translate.by(s, d, Intro.dragTopValue, Interp.pow3)));
            }});
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        subtitleFont.dispose();
    }
}
