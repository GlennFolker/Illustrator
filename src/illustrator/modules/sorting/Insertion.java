package illustrator.modules.sorting;

import arc.freetype.FreeTypeFontGenerator.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import illustrator.*;
import illustrator.Start.*;
import illustrator.entity.*;
import illustrator.graphics.*;
import illustrator.keyframe.*;

import static arc.Core.*;
import static illustrator.Illustrator.*;

public class Insertion implements IllustratorModule {
    private static Font titleFont, subtitleFont;

    private Keyframe firstClick, finish;

    @Override
    public void init() {
        titleFont = inst.fonts.bold.generateFont(new FreeTypeFontParameter() {{ size = 192; }});
        subtitleFont = inst.fonts.semibold.generateFont(new FreeTypeFontParameter() {{ size = 96; }});

        Entity blow, title, concepts;
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
            .child(title = new Text(Ref.when(blow), titleFont) {{
                localTrns.scale.set(0f, 0f);
                color.set(Palettes.lightestGreen);

                text = "Insertion Sort";
                updateLayout();

                var initZoomIn = key(Scale.to(new Immediate(), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(Scale.to(Ref.after(initZoomIn), 12f, new Vec2(1f, 1f), Interp.pow2));

                firstClick = key(Translate.to(new Click(), 30f, new Vec2(0f, camera.position.y + camera.height / 2f - height / 2f - 80f), Interp.smooth2));
            }})
            .child(concepts = new Entity(Ref.when(firstClick, 16f)) {{
                Text insert, compare;
                child(insert = new Text(new Immediate(), subtitleFont) {{
                    localTrns.translation.set(-640f, 0f);
                    localTrns.scale.set(0f, 0f);
                    color.set(Palettes.lightGreen);

                    text = "Insert";
                    valign = Align.bottom;
                    updateLayout();

                    var initZoomIn = key(Scale.to(new Immediate(), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
                    key(Scale.to(Ref.after(initZoomIn), 12f, new Vec2(1f, 1f), Interp.pow2));
                }});
                child(compare = new Text(Ref.when(insert, 8f), subtitleFont) {{
                    localTrns.scale.set(0f, 0f);
                    color.set(Palettes.lightGreen);

                    text = "Compare";
                    valign = Align.bottom;
                    updateLayout();

                    var initZoomIn = key(Scale.to(new Immediate(), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
                    key(Scale.to(Ref.after(initZoomIn), 12f, new Vec2(1f, 1f), Interp.pow2));
                }});
                child(new Text(Ref.when(compare, 8f), subtitleFont) {{
                    localTrns.translation.set(640f, 0f);
                    localTrns.scale.set(0f, 0f);
                    color.set(Palettes.lightGreen);

                    text = "Swap";
                    valign = Align.bottom;
                    updateLayout();

                    var initZoomIn = key(Scale.to(new Immediate(), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
                    key(Scale.to(Ref.after(initZoomIn), 12f, new Vec2(1f, 1f), Interp.pow2));
                }});
            }});

        Entity remover;
        inst.root
            .child(remover = new Entity(new Click()) {{
                onEnter(() -> {
                    title
                        .key(Scale.to(new Immediate(), 16f, Vec2.ZERO, Interp.smooth2))
                        .onExit(title::remove);
                    concepts.eachChildren(concept -> concept
                        .key(Scale.to(new Immediate(), 16f, Vec2.ZERO, Interp.smooth2))
                        .onExit(title::remove)
                    );
                });

                key(new Offset(16f), 1f, (keyframe, lastTime) -> remove());
            }})
            .child(new Entity(Ref.after(remover)) {{
                record Num(Text text, int num) {
                    Num(int num) {
                        this(new Text(new Immediate(), subtitleFont) {
                            {
                                localTrns.scale.set(0f, 0f);
                                color.set(Palettes.white);

                                text = Integer.toString(num);
                                updateLayout();
                            }

                            @Override
                            public void drawSelf(float lastTime) {
                                Draw.z(z);

                                float s = (height + 64f) * globalTrns.scale.y;
                                Draw.color(Palettes.lightGreen);
                                Draws.roundRect(globalTrns.translation, s, s, 0f, s / 4f);

                                super.drawSelf(lastTime);
                            }
                        }, num);
                    }
                }

                Num[] data = {new Num(1), new Num(3), new Num(-6), new Num(5), new Num(7), new Num(-3), new Num(9), new Num(2), new Num(3), new Num(-2)};
                float pad = 32f, width = data[0].text.height + 64f;
                float totalWidth = width * data.length + pad * (data.length - 1);
                float leftMost = -totalWidth / 2f + width / 2f;

                var moveUp = key(new Click(), 1f, (keyframe, lastTime) -> {});
                for(int i = 0; i < data.length; i++) {
                    var num = data[i];
                    child(num.text);

                    num.text.localTrns.translation.set(leftMost + i * (width + pad), 0f);
                    num.text.key(Scale.to(new Offset(i * 4f), 8f, new Vec2(1f, 1f), Interp.pow4));
                    num.text.key(Translate.by(Ref.when(moveUp), 30f, new Vec2(0f, 240f), Interp.smooth2));
                }

                float[] progress = {0f};

                Entity cursor;
                child(cursor = new Entity(new Offset(16f)) {
                    {
                        localTrns.translation.set(leftMost - width / 2f - pad / 2f, 0f);
                        color.set(Palettes.lighterGreen);

                        key(new Immediate(), 32f, (keyframe, lastTime) -> progress[0] = Interp.smooth2.apply(keyframe.time()));
                        key(Translate.by(Ref.when(moveUp), 30f, new Vec2(0f, 240f), Interp.smooth2));
                    }

                    @Override
                    public void drawSelf(float lastTime) {
                        Draw.z(z);
                        Lines.stroke(12f, color);

                        Lines.lineAngle(globalTrns.translation.x, globalTrns.translation.y + width / 2f, -90f, width * progress[0], false);
                    }
                });

                Intc insert = index -> data[index].text.key(Translate.by(new Click(), 30f, new Vec2(0f, -480f), Interp.smooth2));
                Prov<Keyframe> next = () -> cursor.key(Translate.by(new Click(), 20f, new Vec2(width + pad, 0f), Interp.smooth2));
                Intc swap = index -> {
                    var key = data[index].text.key(Translate.by(new Click(), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[index - 1].text.key(Translate.by(Ref.when(key), 24f, new Vec2(width + pad, 0f), Interp.pow3));

                    var old = data[index];
                    data[index] = data[index - 1];
                    data[index - 1] = old;
                };

                insert.get(0);
                next.get();

                insert.get(1);
                next.get();

                insert.get(2);
                swap.get(2);
                swap.get(1);
                next.get();

                insert.get(3);
                next.get();

                insert.get(4);
                next.get();

                insert.get(5);
                swap.get(5);
                swap.get(4);
                swap.get(3);
                swap.get(2);
                next.get();

                insert.get(6);
                next.get();

                insert.get(7);
                swap.get(7);
                swap.get(6);
                swap.get(5);
                swap.get(4);
                next.get();

                insert.get(8);
                swap.get(8);
                swap.get(7);
                swap.get(6);
                next.get();

                insert.get(9);
                swap.get(9);
                swap.get(8);
                swap.get(7);
                swap.get(6);
                swap.get(5);
                swap.get(4);
                swap.get(3);
                var done = next.get();

                cursor
                    .key(Ref.after(done), 32f, (keyframe, lastTime) -> progress[0] = 1f - Interp.smooth2.apply(keyframe.time()))
                    .onExit(cursor::remove);

                for(var num : data) {
                    num.text.key(Translate.by(Ref.after(done), 30f, new Vec2(0f, 240f), Interp.smooth2));
                }

                finish = key(new Click(), 1f, (keyframe, lastTime) -> {});
                for(int i = 0; i < data.length; i++) {
                    var num = data[i];
                    num.text.key(Scale.to(Ref.when(finish, i * 4f), 8f, new Vec2(0f, 0f), Interp.pow4));
                }
            }})
            .child(new Text(Ref.when(finish, 10f * 4f), titleFont) {{
                localTrns.scale.set(0f, 0f);
                color.set(Palettes.lightestGreen);

                text = "Thank You!";

                var initZoomIn = key(Scale.to(new Immediate(), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(Scale.to(Ref.after(initZoomIn), 12f, new Vec2(1f, 1f), Interp.pow2));
            }})
            .child(new Entity(new Click()));
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        subtitleFont.dispose();
    }
}
