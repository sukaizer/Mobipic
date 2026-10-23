package com.mobipic.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Asks what to do with unsaved changes before they would be lost.
 */
public final class ConfirmDialogController {

    public enum Choice { SAVE, DISCARD, CANCEL }

    @FXML
    private Label label;

    private Choice choice = Choice.CANCEL;

    public void init(String message) {
        label.setText(message);
    }

    public Choice getChoice() {
        return choice;
    }

    @FXML
    private void save() {
        close(Choice.SAVE);
    }

    @FXML
    private void discard() {
        close(Choice.DISCARD);
    }

    @FXML
    private void cancel() {
        close(Choice.CANCEL);
    }

    private void close(Choice selected) {
        choice = selected;
        label.getScene().getWindow().hide();
    }
}
