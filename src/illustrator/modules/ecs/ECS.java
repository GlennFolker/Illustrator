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

        illustrator.root
            .color(Color.white)
            .child(new Text(new Immediate(), titleFont) {
                Keyframe lineAppear, lineDisappear, circleDisappear;

                {
                    localTrns.translation.set(0f, 0f);
                    localTrns.scale.set(0f, 0f);
                    color.set(Palettes.green);

                    text = "ECS";
                    float radius = 240f;

                    // Pop out.
                    var initZoomIn = key(new TrnsKeyframe(new Immediate(), 8f, Vec2.ZERO, 0f, new Vec2(1.1f, 1.1f), Interp.pow3));
                    key(new TrnsKeyframe(new After<>(initZoomIn), 8f, Vec2.ZERO, 0f, new Vec2(-0.1f, -0.1f), Interp.pow2));

                    child(new Entity(new After<>(initZoomIn)) {
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
                            key(new TrnsKeyframe(new Immediate(), cAppear + lAppear, Vec2.ZERO, 45f, Vec2.ZERO, Interp.pow3));

                            // Appear.
                            var cAppearKey = key(new Immediate(), cAppear, (keyframe, lastTime) -> circle = Interp.pow2In.apply(keyframe.time()));
                            lineAppear = key(new After<>(cAppearKey), lAppear, (keyframe, lastTime) -> line = Interp.pow3Out.apply(keyframe.time()));

                            // Disappear.
                            lineDisappear = key(new After<>(lineAppear, 20f), lAppear, (keyframe, lastTime) -> line = 1f - Interp.pow3In.apply(keyframe.time()));
                            circleDisappear = key(new After<>(lineDisappear), cAppear, (keyframe, lastTime) -> circle = 1f - Interp.pow4.apply(keyframe.time()));

                            // Rotate to disappear.
                            key(new TrnsKeyframe(new After<>(lineAppear, 6f), 40f, Vec2.ZERO, -90f, Vec2.ZERO, Interp.smooth2));
                            key(new TrnsKeyframe(new After<>(lineDisappear), circleDisappear.duration, Vec2.ZERO, 60f, Vec2.ZERO, Interp.smooth2));
                        }
                    });

                    // Drag to top-left based on radius.
                    key(new TrnsKeyframe(new After<>(lineAppear, 20f), 30f, new Vec2(
                        -videoWidth / 2f + radius + 48f,
                        videoHeight / 2f - radius - 48f
                    ), 0f, Vec2.ZERO, Interp.smooth2));

                    // Drag to top-left based on layout size.
                    key(new WrapKeyframe(new After<>(circleDisappear), 30f, (start, duration) -> new TrnsKeyframe(start, duration, new Vec2(
                        0f,
                        (-videoWidth / 2f - globalTrns.translation.x + width / 2f) + videoHeight / 2f - globalTrns.translation.y - height / 2f
                    ), 0f, Vec2.ZERO, Interp.pow3)));

                    var title = this;
                    child(new Text(new After<>(lineDisappear, 25f), subtitleFont) {{
                        color.set(Palettes.darkGreen);

                        halign = Align.left;
                        valign = Align.top;

                        onEnter(() -> localTrns.translation.set(-title.width / 2f, -title.height / 2f - 24f));

                        // Typing the subtitle out for 3/4 seconds.
                        key(new TypingKeyframe(new Immediate(), 45f, "", "Entity Component System"));
                    }});
                }
            });
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        subtitleFont.dispose();
    }
}
