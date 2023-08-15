package illustrator.modules;

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
    private Font titleFont;

    @Override
    public void init(Illustrator illustrator) {
        titleFont = illustrator.fonts.semibold.generateFont(new FreeTypeFontParameter() {{
            size = 192;
        }});

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
                key(new TrnsKeyframe(0f, 8f, Vec2.ZERO, 0f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(new TrnsKeyframe(8f, 16f, Vec2.ZERO, 0f, new Vec2(-0.1f, -0.1f), Interp.pow2));

                key(new TrnsKeyframe(120f, 150f, new Vec2(
                    -videoWidth / 2f + radius + 48f,
                    videoHeight / 2f - radius - 48f
                ), 0f, Vec2.ZERO, Interp.smooth2));

                child(new Entity(16f) {
                    float circle;
                    float line;

                    {
                        color.set(Palettes.green);
                        key(0f, 52f, (entity, keyframe, lastTime) -> circle = Interp.pow2In.apply(keyframe.time()));
                        key(52f, 104f, (entity, keyframe, lastTime) -> line = Interp.pow3Out.apply(keyframe.time()));

                        key(new TrnsKeyframe(86f, 126f, Vec2.ZERO, -90f, Vec2.ZERO, Interp.smoother));
                    }

                    @Override
                    public void draw(float lastTime) {
                        Draw.color(color);
                        Lines.stroke(24f);
                        Lines.arc(globalTrns.translation.x, globalTrns.translation.y, radius, circle * 350f / 360f, 55f + globalTrns.rotation);

                        Tmp.v1.trns(45f + globalTrns.rotation, radius).add(globalTrns.translation);
                        Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, 45f + globalTrns.rotation, 120f * line);
                    }
                });
            }});
    }

    @Override
    public void dispose() {
        titleFont.dispose();
    }
}
