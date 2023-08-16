package illustrator;

import arc.*;
import arc.backend.sdl.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import illustrator.Start.*;
import illustrator.modules.ecs.*;

import java.io.*;
import java.util.concurrent.locks.*;

import static arc.Core.*;

public class Illustrator implements ApplicationListener {
    public static final int videoWidth = 1920, videoHeight = 1080, samples = 4;
    private final IllustratorModule module;

    public Root root;
    public Fonts fonts;
    private FrameBuffer buffer, upscale;
    private Shader screenspace;
    private float lastTime = -1f;

    private final Queue<byte[]> frames = new Queue<>();
    private boolean done = false;
    private Lock doneLock;
    private Condition doneCond;
    private Thread frameWorker;

    private static boolean preview;
    private static boolean ffmpegOutput;

    public static void main(String[] args) {
        preview = Structs.contains(args, "--preview");
        ffmpegOutput = Structs.contains(args, "--ffmpeg-output");

        new SdlApplication(new Illustrator(new ECS()), new SdlConfig() {{
            if(preview) {
                width = videoWidth;
                height = videoHeight;
            } else {
                width = 0;
                height = 0;
            }

            resizable = false;
            decorated = false;
            disableAudio = true;

            title = "Illustrator Process";
            initialBackgroundColor = Color.clear;
            initialVisible = false;
            vSyncEnabled = false;
        }});
    }

    public Illustrator(IllustratorModule module) {
        this.module = module;
    }

    @Override
    public void init() {
        Lines.setCirclePrecision(8f);

        camera = new Camera();
        camera.resize(videoWidth, videoHeight);

        batch = new SortedSpriteBatch();
        atlas = TextureAtlas.blankAtlas();

        root = new Root();
        fonts = new Fonts();
        buffer = new FrameBuffer(videoWidth, videoHeight);
        upscale = new FrameBuffer(videoWidth * samples, videoHeight * samples);
        screenspace = new Shader("""
            attribute vec2 a_position;
            attribute vec2 a_texCoord0;
            
            varying vec2 v_texCoords;
            
            void main() {
                gl_Position = vec4(a_position, 0., 1.);
                v_texCoords = a_texCoord0;
            }
            """, """
            uniform sampler2D u_texture;
            
            varying vec2 v_texCoords;
            
            void main() {
                gl_FragColor = texture2D(u_texture, v_texCoords);
            }
            """
        );

        if(!preview) {
            doneLock = new ReentrantLock();
            doneCond = doneLock.newCondition();
            frameWorker = new Thread(() -> {
                var output = files.local("output.mp4");
                output.delete();

                try {
                    var process = new ProcessBuilder(
                        "ffmpeg", "-hide_banner", "-loglevel", "error",
                        "-framerate", "60",
                        "-s", videoWidth + "x" + videoHeight,
                        "-f", "rawvideo",
                        "-pix_fmt", "rgba",
                        "-i", "-",
                        "-codec:v", "libx264", "-preset", "veryslow", "-crf", "17", "-tune", "animation",
                        "-profile:v", "baseline", "-pix_fmt", "yuv420p",
                        "-filter:v", "vflip",
                        output.absolutePath()
                    ).start();

                    Thread errPrinter = null;
                    if(ffmpegOutput) {
                        errPrinter = new Thread(() -> {
                            try {
                                var err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                String line;
                                while((line = err.readLine()) != null) {
                                    System.out.println(line);
                                }
                            } catch(IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, "FFmpeg-StdErr-Printer");
                        errPrinter.start();
                    }

                    var out = process.getOutputStream();

                    boolean work = true;
                    while(work) {
                        doneLock.lock();
                        try {
                            if(done) {
                                work = false;
                            } else {
                                doneCond.await();
                                if(done) work = false;
                            }
                        } catch(InterruptedException e) {
                            throw new RuntimeException(e);
                        } finally {
                            doneLock.unlock();
                        }

                        while(true) {
                            byte[] frame;
                            synchronized(frames) {
                                if(frames.isEmpty()) break;
                                frame = frames.removeFirst();
                            }

                            out.write(frame);
                            out.flush();
                        }
                    }

                    out.close();
                    process.waitFor();

                    if(errPrinter != null) errPrinter.join();
                } catch(IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "Frame-Worker");
            frameWorker.start();
        }

        Time.setDeltaProvider(() -> 1f);
        fonts.load();

        module.init(this);
        if(!preview) {
            buffer.begin();
            Log.info("Recording...");
        }
    }

    @Override
    public void update() {
        long started = Time.millis();

        Time.updateGlobal();
        Time.update();
        root.update(lastTime);

        upscale.begin();

        camera.update();
        Draw.proj(camera.mat);
        Draw.sort(true);

        root.draw(lastTime);

        Draw.sort(false);
        Draw.flush();

        upscale.end();
        Draw.blit(upscale.getTexture(), screenspace);

        if(!preview) {
            synchronized(frames) {
                frames.addLast(ScreenUtils.getFrameBufferPixels(0, 0, videoWidth, videoHeight, false));
            }
            doneLock.lock();
            doneCond.signal();
            doneLock.unlock();
        }

        lastTime = Time.time;
        if(preview) {
            long elapsed = Time.millis() - started;
            long rate = (long)(1000d / 60d);
            if(elapsed < rate) {
                Threads.sleep(rate - elapsed);
            }
        }

        if(root.isCompleted()) app.exit();
    }

    @Override
    public void dispose() {
        upscale.dispose();
        screenspace.dispose();

        if(!preview) buffer.end();
        buffer.dispose();

        module.dispose();
        fonts.dispose();

        if(!preview) {
            doneLock.lock();
            done = true;
            doneCond.signalAll();
            doneLock.unlock();

            Log.info("Waiting for FFmpeg to finish...");
            try {
                frameWorker.join();
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface IllustratorModule extends Disposable {
        void init(Illustrator illustrator);
    }

    public static class Root extends Entity {
        public Root() {
            super(new Immediate());
        }

        @Override
        public void drawSelf(float lastTime) {
            graphics.clear(color);
        }
    }
}
