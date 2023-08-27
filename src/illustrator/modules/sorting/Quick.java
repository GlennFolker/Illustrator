package illustrator.modules.sorting;

import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import illustrator.*;
import illustrator.Start.*;
import illustrator.entity.*;
import illustrator.graphics.*;
import illustrator.keyframe.*;

import static arc.Core.*;
import static illustrator.Illustrator.*;

public class Quick implements IllustratorModule {
    private static Font titleFont, subtitleFont;

    private static Keyframe firstClick;

    @Override
    public void init() {
        titleFont = inst.fonts.bold.generateFont(new FreeTypeFontParameter() {{ size = 192; }});
        subtitleFont = inst.fonts.semibold.generateFont(new FreeTypeFontParameter() {{ size = 72; }});

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
        } Num[] data = {new Num(1), new Num(3), new Num(-6), new Num(7), new Num(5), new Num(-3), new Num(9), new Num(3), new Num(2), new Num(-2)};
        float pad = 32f, width = data[0].text.height + 56f;

        var sorted = data.clone();
        Sort.instance().sort(sorted, Structs.comparingInt(n -> n.num));

        class Line extends Entity {
            Entity a, b;
            float progress;

            Line(Start start, Entity a, Entity b) {
                super(start);
                this.a = a;
                this.b = b;

                z = -50f;
                color.set(Palettes.lighterGreen);
                key(new Immediate(), 60f, (keyframe, lastTime) -> progress = Interp.smooth2.apply(keyframe.time()));
            }

            @Override
            public void drawSelf(float lastTime) {
                Draw.z(z);
                Lines.stroke(12f);
                Draw.color(color, progress);
                Lines.line(a.globalTrns.translation.x, a.globalTrns.translation.y, b.globalTrns.translation.x, b.globalTrns.translation.y);
            }
        }

        Entity blow, container;
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
            .child(new Text(Ref.when(blow), titleFont) {{
                localTrns.scale.set(0f, 0f);
                color.set(Palettes.lightestGreen);

                text = "Quick Sort";
                updateLayout();

                var initZoomIn = key(Scale.to(new Immediate(), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
                key(Scale.to(Ref.after(initZoomIn), 12f, new Vec2(1f, 1f), Interp.pow2));

                (firstClick = key(Scale.to(new Click(), 30f, Vec2.ZERO, Interp.smooth2))).onExit(this::remove);
            }})
            .child(container = new Entity(Ref.after(firstClick)) {{
                float totalWidth = width * data.length + pad * (data.length - 1);
                float leftMost = -totalWidth / 2f + width / 2f;

                var moveUp = key(new Click(), 1f, (keyframe, lastTime) -> {});
                for(int i = 0; i < data.length; i++) {
                    var num = data[i];
                    child(num.text);

                    num.text.localTrns.translation.set(leftMost + i * (width + pad), 0f);
                    num.text.key(Scale.to(new Offset(i * 4f), 8f, new Vec2(1f, 1f), Interp.pow4));
                    num.text.key(new Wrap(Ref.when(moveUp), 30f, (s, d) -> Translate.to(s, d, new Vec2(num.text.localTrns.translation.x, videoHeight / 2f - pad - width / 2f), Interp.smooth2)));
                }

                interface Prepare {
                    Keyframe get(int begin, int end);
                } Prepare prepare = (begin, end) -> {
                    var iter = key(new Click(), 1f, (keyframe, lastTime) -> {});
                    data[end - 1].text.key(Translate.by(Ref.when(iter, 6f), 18f, new Vec2((-width - pad) * (end - begin - 1f) / 2f, 0f), Interp.pow3));

                    for(int i = begin; i < end - 1; i++) {
                        var num = data[i];
                        var down = num.text.key(Translate.by(Ref.when(iter), 12f, new Vec2(0f, -width - pad - width - pad), Interp.pow3In));
                        num.text.key(Translate.by(Ref.after(down), 12f, new Vec2(width / 2f, 0f), Interp.pow3Out));
                    }

                    return iter;
                };

                // 1, 3, -6, 7, 5, -3, 9, 3, 2, -2.
                prepare.get(0, 10);

                {
                    var num = data[0];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));

                    System.arraycopy(data, 1, data, 0, data.length - 1 - 1);
                    data[data.length - 1 - 1] = num;
                }

                {
                    var num = data[0];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 1 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 1, data, 0, data.length - 1 - 1);
                    data[data.length - 1 - 1] = num;
                }

                {
                    var num = data[0];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(-videoWidth / 2f + width / 2f + pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                }

                {
                    var num = data[1];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 1 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 2, data, 1, data.length - 1 - 2);
                    data[data.length - 1 - 1] = num;
                }

                {
                    var num = data[1];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 1 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 3].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 2, data, 1, data.length - 1 - 2);
                    data[data.length - 1 - 1] = num;
                }

                {
                    var num = data[1];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(-videoWidth / 2f + width / 2f + pad + width + pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                }

                {
                    var num = data[2];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 1 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 3].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 4].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 3, data, 2, data.length - 1 - 3);
                    data[data.length - 1 - 1] = num;
                }

                {
                    var num = data[2];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 1 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 3].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 4].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 5].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 3, data, 2, data.length - 1 - 3);
                    data[data.length - 1 - 1] = num;
                }

                {
                    var num = data[2];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(videoWidth / 2f - width / 2f - pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 1 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 3].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 4].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 5].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 1 - 6].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 3, data, 2, data.length - 1 - 3);
                    data[data.length - 1 - 1] = num;
                }

                // 0 <- Begin.
                // -6, -3.
                child(new Line(Ref.when(prepare.get(0, 2), 24f), data[9].text, data[1].text));

                {
                    var num = data[0];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(-videoWidth / 2f + width / 2f + pad, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    child(new Line(Ref.when(move, 24f), data[1].text, data[0].text));
                }

                // 2 <- Begin.
                // 1, 3, 7, 5, 9, 3, 2.
                child(new Line(Ref.when(prepare.get(2, 9), 24f), data[9].text, data[8].text));
                float[] iLeftMost = {0f, 0f, 0f};
                float[] iRightMost = {videoWidth / 2f - pad - width / 2f, 0f};

                {
                    var num = data[2];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iLeftMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                }

                {
                    var num = data[3];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));

                    System.arraycopy(data, 4, data, 3, data.length - 2 - 4);
                    data[data.length - 2 - 1] = num;
                }

                {
                    var num = data[3];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 2 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 4, data, 3, data.length - 2 - 4);
                    data[data.length - 2 - 1] = num;
                }

                {
                    var num = data[3];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 2 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 2 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 4, data, 3, data.length - 2 - 4);
                    data[data.length - 2 - 1] = num;
                }

                {
                    var num = data[3];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 2 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 2 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 2 - 3].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 4, data, 3, data.length - 2 - 4);
                    data[data.length - 2 - 1] = num;
                }

                {
                    var num = data[3];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 2 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 2 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 2 - 3].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 2 - 4].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 4, data, 3, data.length - 2 - 4);
                    data[data.length - 2 - 1] = num;
                }

                // 3 <- Begin.
                // 3, 7, 5, 9, 3.
                {
                    var preparation = prepare.get(3, 8);
                    child(new Line(Ref.when(preparation, 24f), data[8].text, data[2].text));
                    child(new Line(Ref.when(preparation, 24f), data[8].text, data[7].text));
                }

                iLeftMost[1] = iLeftMost[0] + (pad + width) * 2f;

                {
                    var num = data[3];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iLeftMost[1], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                }

                {
                    var num = data[4];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));

                    System.arraycopy(data, 5, data, 4, data.length - 3 - 5);
                    data[data.length - 3 - 1] = num;
                }

                {
                    var num = data[4];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 3 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 5, data, 4, data.length - 3 - 5);
                    data[data.length - 3 - 1] = num;
                }

                {
                    var num = data[4];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[0], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    data[data.length - 3 - 1].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));
                    data[data.length - 3 - 2].text.key(Translate.by(Ref.when(move), 24f, new Vec2(-width - pad, 0f), Interp.pow3));

                    System.arraycopy(data, 5, data, 4, data.length - 3 - 5);
                    data[data.length - 3 - 1] = num;
                }

                // 4 <- Begin.
                // 7, 5, 9.
                {
                    var preparation = prepare.get(4, 7);
                    child(new Line(Ref.when(preparation, 24f), data[7].text, data[3].text));
                    child(new Line(Ref.when(preparation, 24f), data[7].text, data[6].text));
                }

                iLeftMost[2] = iLeftMost[1] + (pad + width) * 2f;

                {
                    var num = data[4];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iLeftMost[2], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                }

                {
                    var num = data[5];
                    var text = num.text;
                    text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iLeftMost[2] + pad + width, text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                }

                // 4 <- Begin.
                // 7, 5.
                child(new Line(Ref.when(prepare.get(4, 6), 24f), data[6].text, data[5].text));
                iRightMost[1] = iRightMost[0] - pad - width;

                {
                    var num = data[4];
                    var text = num.text;
                    var move = text.key(new Wrap(new Click(), 24f, (s, d) -> Translate.to(s, d, new Vec2(iRightMost[1], text.localTrns.translation.y + width + pad), Interp.pow3Out)));
                    child(new Line(Ref.when(move, 24f), data[5].text, data[4].text));
                }
            }})
            .child(new Entity(new Click()) {{
                boolean first = true;
                for(var num : sorted) {
                    num.text.key(Scale.to(first ? Ref.when(this) : new Click(), 16f, new Vec2(1.5f, 1.5f), Interp.pow3));
                    first = false;
                }
            }})
            .child(new Entity(new Click()) {{
                onEnter(() -> container.eachChildren(e -> {
                    if(e instanceof Line line) e
                        .key(new Immediate(), 12f, (keyframe, lastTime) -> line.progress = 1f - Interp.smooth2.apply(keyframe.time()))
                        .onExit(line::remove);
                }));

                for(int i = 0; i < sorted.length; i++) {
                    var num = sorted[i];
                    var move = num.text.key(Translate.to(Ref.when(this, 12f + i * 6f), 24f, new Vec2(
                        -(width * sorted.length + pad * (sorted.length - 1)) / 2f + width / 2f + (pad + width) * i,
                        0f
                    ), Interp.pow3));
                    num.text.key(Scale.to(Ref.when(move), 16f, new Vec2(1f, 1f), Interp.pow2));
                }
            }})
            .child(new Text(new Click(), titleFont) {{
                onEnter(() -> {
                    for(int i = 0; i < sorted.length; i++) {
                        var num = sorted[i];
                        num.text.key(Scale.to(Ref.when(this, i * 4f), 8f, new Vec2(0f, 0f), Interp.pow4));
                    }
                });

                localTrns.scale.set(0f, 0f);
                color.set(Palettes.lightestGreen);

                text = "Thank You!";

                var initZoomIn = key(Scale.to(new Offset(10f * 4f), 12f, new Vec2(1.1f, 1.1f), Interp.pow3));
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
