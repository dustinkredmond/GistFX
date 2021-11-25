package com.dustinredmond.ui;

import com.dustinredmond.javafx.CustomAlert;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UI extends Application {

    @Override
    public void start(Stage stage) {
        UI.stage = stage;
        stage.setTitle(APP_TITLE);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
        CustomAlert.setIconPath(getClass().getResource(APP_ICON).toExternalForm());
        CustomAlert.setApplicationTitle(APP_TITLE);
        stage.setScene(new Scene(new Group()));
        new LoginWindow();
        stage.show();
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
    public static final String APP_ICON = "icons8-repository-48.png";
    //Comment
}
