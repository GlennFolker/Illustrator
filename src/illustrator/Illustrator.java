package illustrator;

import arc.*;
import arc.backend.sdl.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import illustrator.modules.*;

import java.io.*;
import java.util.concurrent.locks.*;

import static arc.Core.*;

public class Illustrator implements ApplicationListener {
    public static final int videoWidth = 1920, videoHeight = 1080, samples = 4;

    private final IllustratorModule module;

    public Entities entities;
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

    public static void main(String[] args) {
        if(args.length >= 1 && args[0].equals("--preview")) {
            preview = true;
        }

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
        Lines.setCirclePrecision(5f);

        camera = new Camera();
        camera.resize(videoWidth, videoHeight);

        batch = new SortedSpriteBatch();
        atlas = TextureAtlas.blankAtlas();

        entities = new Entities();
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
            """);

        if(!preview) {
            doneLock = new ReentrantLock();
            doneCond = doneLock.newCondition();
        }

        var output = files.local("output.mp4");
        output.delete();

        if(!preview) frameWorker = new Thread(() -> {
            try {
                var process = new ProcessBuilder(
                    "ffmpeg", "-hide_banner", "-loglevel", "error",
                    "-framerate", "60",
                    "-s", videoWidth + "x" + videoHeight,
                    "-f", "rawvideo",
                    "-pix_fmt", "rgba",
                    "-i", "-",
                    "-filter:v", "vflip",
                    output.absolutePath()
                ).start();

                /*new Thread(() -> {
                    var err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    try {
                        while((line = err.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();*/

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
            } catch(IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Frame-Worker");
        if(!preview) frameWorker.start();

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
        entities.update(lastTime);

        graphics.clear(Color.clear);
        upscale.begin(Color.clear);

        camera.update();
        Draw.proj(camera.mat);
        Draw.sort(true);

        if(entities.draw(lastTime)) app.exit();

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

            Log.info("Finished.");
        }
    }

    public interface IllustratorModule extends Disposable {
        void init(Illustrator illustrator);
    }
}
