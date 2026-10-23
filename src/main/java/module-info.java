/**
 * Mobipic, a basic photo editor written with JavaFX.
 */
module com.mobipic {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    exports com.mobipic.app;
    exports com.mobipic.model;

    opens com.mobipic.controller to javafx.fxml;
    opens com.mobipic.io.dto to com.fasterxml.jackson.databind;

    // Resource-only packages loaded by FXML / CSS at runtime.
    opens com.mobipic.fxml;
    opens com.mobipic.css;
    opens com.mobipic.images;
    opens com.mobipic.i18n;
}
