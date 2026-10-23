package com.mobipic.util;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

/**
 * Helpers to show modal windows owned by the main window.
 */
public final class Dialogs {

    private static final System.Logger LOG = System.getLogger(Dialogs.class.getName());

    private Dialogs() {
    }

    /**
     * Shows an FXML view in a modal window and waits until it is closed.
     *
     * @param init called with the controller before the window is shown
     * @return the controller, to read the result
     */
    public static <C> C showModal(Window owner, String fxml, String titleKey, Consumer<C> init) {
        Fxml.View<C> view = Fxml.load(fxml);
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(I18n.get(titleKey));
        stage.setResizable(false);
        stage.setScene(new Scene(view.root()));
        if (owner instanceof Stage ownerStage) {
            stage.getIcons().setAll(ownerStage.getIcons());
        }
        init.accept(view.controller());
        stage.showAndWait();
        return view.controller();
    }

    /** Logs the error and tells the user what failed, with the technical details folded below. */
    public static void error(Window owner, String messageKey, Throwable error) {
        LOG.log(System.Logger.Level.ERROR, I18n.get(messageKey), error);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Mobipic");
        alert.setHeaderText(I18n.get(messageKey));
        alert.setContentText(error.getLocalizedMessage());
        StringWriter trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        TextArea details = new TextArea(trace.toString());
        details.setEditable(false);
        alert.getDialogPane().setExpandableContent(details);
        alert.showAndWait();
    }
}
