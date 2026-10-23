package com.mobipic.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;

/**
 * Loads FXML views and images bundled with the application.
 */
public final class Fxml {

    private static final String ROOT = "/com/mobipic/";

    /** A loaded view and its controller. */
    public record View<C>(Parent root, C controller) {
    }

    private Fxml() {
    }

    /** @param name file name inside the {@code fxml} folder, e.g. {@code "main.fxml"} */
    public static <C> View<C> load(String name) {
        FXMLLoader loader = new FXMLLoader(resource("fxml/" + name), I18n.bundle());
        try {
            Parent root = loader.load();
            return new View<>(root, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load view " + name, e);
        }
    }

    /** @param name file name inside the {@code images} folder */
    public static Image image(String name) {
        return new Image(resource("images/" + name).toExternalForm());
    }

    public static URL resource(String path) {
        return Objects.requireNonNull(Fxml.class.getResource(ROOT + path), () -> "Missing resource " + ROOT + path);
    }
}
