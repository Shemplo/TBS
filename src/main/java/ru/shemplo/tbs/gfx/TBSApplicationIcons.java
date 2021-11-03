package ru.shemplo.tbs.gfx;

import java.util.Optional;

import javafx.scene.image.Image;

public class TBSApplicationIcons {
    
    public static final Image window = loadIcon ("search.png");
    public static final Image customize = loadIcon ("customize.png");
    public static final Image warning = loadIcon ("warning.png");

    private static Image loadIcon (String name) {
        final var path = String.format ("%s/%s", "", name);
        final var is = TBSUIApplication.class.getResourceAsStream (path);
        return Optional.ofNullable (is).map (Image::new).orElse (null);
    }
    
}
