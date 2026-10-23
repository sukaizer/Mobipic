package com.mobipic.controller;

import com.mobipic.FxToolkit;
import com.mobipic.tool.ToolSettings;
import com.mobipic.util.Fxml;
import javafx.scene.Parent;
import javafx.scene.control.Spinner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionsBarTest {

    /** Regression: spinners were bound through temporary properties that the garbage collector removed. */
    @Test
    void spinnersKeepFollowingTheSettingsAfterGarbageCollection() throws Exception {
        ToolSettings settings = FxToolkit.onFx(ToolSettings::new);
        Parent root = FxToolkit.onFx(() -> {
            Fxml.View<OptionsBarController> view = Fxml.load("optionsBar.fxml");
            view.controller().init(settings);
            return view.root();
        });
        for (int i = 0; i < 5; i++) {
            System.gc();
            Thread.sleep(20);
        }
        double[] values = FxToolkit.onFx(() -> {
            settings.lineWidthProperty().set(6);
            settings.cornerRadiusProperty().set(24);
            settings.fontSizeProperty().set(72);
            return new double[]{
                    spinner(root, "#widthSpinner").getValue(),
                    spinner(root, "#radiusSpinner").getValue(),
                    spinner(root, "#sizeSpinner").getValue()};
        });
        assertEquals(6, values[0]);
        assertEquals(24, values[1]);
        assertEquals(72, values[2]);

        FxToolkit.onFx(() -> {
            spinner(root, "#widthSpinner").getValueFactory().setValue(12.0);
            return null;
        });
        assertEquals(12, settings.lineWidthProperty().get());
    }

    @SuppressWarnings("unchecked")
    private static Spinner<Double> spinner(Parent root, String selector) {
        return (Spinner<Double>) root.lookup(selector);
    }
}
