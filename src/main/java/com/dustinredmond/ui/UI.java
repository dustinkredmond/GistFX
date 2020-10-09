package com.dustinredmond.ui;

import com.dustinredmond.javafx.CustomAlert;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UI extends Application {

    @Override
    public void start(Stage stage) {
        UI.stage = stage;
        stage.setTitle(APP_TITLE);
        CustomAlert.setApplicationTitle(APP_TITLE);
        stage.setScene(new Scene(new Group()));
        new LoginWindow();
        stage.show();

        // TODO remove when finished dev
        String todo = "" +
                "1. Implement GistOverviewWindow's add gist function\n" +
                "\t Be sure to wire up the buttons and context menu\n" +
                "2. Implement the Add/Delete GistFiles\n" +
                "\t Create Context Menu and Buttons over ListView\n" +
                "\t Debug exception sometimes thrown when saving GistFile";
        CustomAlert.showInfo(todo);
    }

    public void startUi(String[] args) {
        Application.launch(args);
    }

    private static Stage stage;

    public static Stage getStage() {
        return UI.stage;
    }

    public static void setTitleContext(String context) {
        stage.setTitle(String.format("%s - %s", APP_TITLE, context));
    }

    public static final String APP_TITLE = "GistFX";
}
