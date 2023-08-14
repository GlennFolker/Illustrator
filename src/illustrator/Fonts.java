package illustrator;

import arc.freetype.*;
import arc.util.*;

import static arc.Core.*;

public class Fonts implements Disposable {
    public FreeTypeFontGenerator
        bold, boldItalic,
        semibold, semiboldItalic,
        medium, mediumItalic,
        light, lightItalic;

    public void load() {
        bold = new FreeTypeFontGenerator(files.internal("fonts/bold.ttf"));
        boldItalic = new FreeTypeFontGenerator(files.internal("fonts/bold-italic.ttf"));
        semibold = new FreeTypeFontGenerator(files.internal("fonts/semibold.ttf"));
        semiboldItalic = new FreeTypeFontGenerator(files.internal("fonts/semibold-italic.ttf"));
        medium = new FreeTypeFontGenerator(files.internal("fonts/medium.ttf"));
        mediumItalic = new FreeTypeFontGenerator(files.internal("fonts/medium-italic.ttf"));
        light = new FreeTypeFontGenerator(files.internal("fonts/light.ttf"));
        lightItalic = new FreeTypeFontGenerator(files.internal("fonts/light-italic.ttf"));
    }

    @Override
    public void dispose() {
        bold.dispose();
        boldItalic.dispose();
        semibold.dispose();
        semiboldItalic.dispose();
        medium.dispose();
        mediumItalic.dispose();
        light.dispose();
        lightItalic.dispose();
    }
}
