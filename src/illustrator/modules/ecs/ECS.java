package illustrator.modules.ecs;

import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import illustrator.*;
import illustrator.entity.*;
import illustrator.keyframe.*;

import static illustrator.Illustrator.*;

public class ECS implements IllustratorModule {
    private Font titleFont, subtitleFont;

    @Override
    public void init(Illustrator illustrator) {
        titleFont = illustrator.fonts.bold.generateFont(new FreeTypeFontParameter() {{ size = 192; }});
        subtitleFont = illustrator.fonts.semibold.generateFont(new FreeTypeFontParameter() {{ size = 96; }});

        illustrator.entities
            .add(new Background(0f) {{
                color.set(Color.white);
            }})
            .add(new Text(0f, titleFont) {{
                localTrns.translation.set(0f, 0f);
                localTrns.scale.set(0f, 0f);
                color.set(Palettes.green);

                text = "ECS";

                float radius = 240f;
                // Pop out.
                key(new TrnsKeyframe(0f, 8f, Vec2.ZERO, 0f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(new TrnsKeyframe(8f, 16f, Vec2.ZERO, 0f, new Vec2(-0.1f, -0.1f), Interp.pow2));

                // Drag to top-left.
                key(new TrnsKeyframe(100f, 130f, new Vec2(
                    -videoWidth / 2f + radius + 48f,
                    videoHeight / 2f - radius - 48f
                ), 0f, Vec2.ZERO, Interp.smooth2));

                child(new Entity(16f) {
                    float circle;
                    float line;

                    @Override
                    public void draw(float lastTime) {
                        Lines.stroke(30f, color);
                        Lines.arc(
                            globalTrns.translation.x, globalTrns.translation.y,
                            radius, circle * 350f / 360f, 10f + globalTrns.rotation,
                            Lines.circleVertices(radius)
                        );

                        Tmp.v1.trns(globalTrns.rotation, radius).add(globalTrns.translation);
                        Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, globalTrns.rotation, 120f * line);
                    }

                    {
                        color.set(Palettes.green);

                        // Rotate to appear.
                        float cAppear = 45f, lAppear = 15f;
                        key(new TrnsKeyframe(0f, cAppear + lAppear, Vec2.ZERO, 45f, Vec2.ZERO, Interp.pow3));

                        // Appear.
                        key(0f, cAppear, (entity, keyframe, lastTime) -> circle = Interp.pow2In.apply(keyframe.time()));
                        key(cAppear, cAppear + lAppear, (entity, keyframe, lastTime) -> line = Interp.pow3Out.apply(keyframe.time()));

                        // Rotate to disappear.
                        key(new TrnsKeyframe(66f, 106f, Vec2.ZERO, -90f, Vec2.ZERO, Interp.smooth2));
                        key(new TrnsKeyframe(80f + lAppear, 80f + lAppear + cAppear, Vec2.ZERO, 60f, Vec2.ZERO, Interp.smooth2));

                        // Disappear.
                        key(80f, 80f + lAppear, (entity, keyframe, lastTime) -> line = 1f - Interp.pow3In.apply(keyframe.time()));
                        key(80f + lAppear, 80f + cAppear + lAppear, (entity, keyframe, lastTime) -> circle = 1f - Interp.pow4.apply(keyframe.time()));
                    }
                });

                var title = this;
                child(new Text(130f, subtitleFont) {{
                    color.set(Palettes.darkGreen);

                    halign = Align.left;
                    valign = Align.top;

                    onEnter(() -> localTrns.translation.set(-title.width / 2f, -title.height / 2f - 24f));

                    // Typing the subtitle out for 3/4 seconds.
                    key(new TypingKeyframe(0f, 45f, "", "Entity Component System"));
                    key(0f, 120f, (a, b, c) -> {});
                }});
            }});
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        subtitleFont.dispose();
    }
}
