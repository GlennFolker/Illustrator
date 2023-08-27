package illustrator;

import arc.graphics.*;

public final class Palettes {
    public static Color
        white = new Color(0xe5e4f2ff),
        lighterGray = new Color(0xd1d0e0ff),
        lightGray = new Color(0xa8a7bdff),
        gray = new Color(0x89879eff),
        darkGray = new Color(0x626077ff),
        darkerGray = new Color(0x383746ff),
        black = new Color(0x18181fff),

        lightestGreen = new Color(0x59ffa1ff),
        lighterGreen = new Color(0x29e9b1ff),
        lightGreen = new Color(0x10cdb4ff),

        lightestBlue = new Color(0x59f8ffff),
        lighterBlue = new Color(0x29bfe9ff),
        lightBlue = new Color(0x1083cdff);

    private Palettes() {
        throw new AssertionError();
    }
}
