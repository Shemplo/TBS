package ru.shemplo.tbs.gfx;

import java.util.Optional;

import javafx.scene.image.Image;

public class TBSApplicationIcons {
    
    public static final Image window = loadIcon ("search.png");
    public static final Image customize = loadIcon ("customize.png");
    public static final Image approved = loadIcon ("approved.png");
    public static final Image approved24 = loadIcon ("approved24.png");
    public static final Image warning = loadIcon ("warning.png");
    public static final Image warning24 = loadIcon ("warning24.png");
    public static final Image error = loadIcon ("error.png");
    public static final Image error24 = loadIcon ("error24.png");
    public static final Image fatal = loadIcon ("fatal.png");
    public static final Image fatal24 = loadIcon ("fatal24.png");
    public static final Image excel = loadIcon ("excel.png");
    public static final Image sync = loadIcon ("sync.png");
    public static final Image sync24 = loadIcon ("sync24.png");
    public static final Image signal = loadIcon ("signal.png");
    public static final Image range = loadIcon ("range.png");
    public static final Image question = loadIcon ("question.png");

    private static Image loadIcon (String name) {
        final var path = String.format ("%s/%s", "", name);
        final var is = TBSUIApplication.class.getResourceAsStream (path);
        return Optional.ofNullable (is).map (Image::new).orElse (null);
    }
    
}
