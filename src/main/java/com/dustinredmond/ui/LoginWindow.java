package com.dustinredmond.ui;

import com.dustinredmond.javafx.CustomAlert;
import com.dustinredmond.javafx.PaddedGridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class LoginWindow {

    public LoginWindow() {
        GridPane grid = new PaddedGridPane(5, 10);
        UI.setTitleContext("Login");

        int rowIndex = 0;

        TextField textFieldUser = new TextField();
        textFieldUser.setPromptText("GitHub username");
        grid.add(new Label("Username:"), 0, rowIndex);
        grid.add(textFieldUser, 1, rowIndex++);

        PasswordField textFieldPass = new PasswordField();
        textFieldPass.setPromptText("GitHub password");
        grid.add(new Label("Password:"), 0, rowIndex);
        grid.add(textFieldPass, 1, rowIndex++);

        Button buttonLogin = new Button("Login to GitHub");
        buttonLogin.setOnAction(e -> {
            if (controller.authenticate(textFieldUser, textFieldPass)) {
                new GistOverviewWindow();
            } else {
                textFieldUser.setText("");
                textFieldPass.setText("");
                CustomAlert.showWarning("Unable to authenticate with provided credentials.");
            }
        });
        grid.add(buttonLogin, 0, rowIndex);

        UI.getStage().getScene().setRoot(grid);

        grid.requestFocus(); // So user can see first TextField's prompt text
    }

    private static final LoginWindowController controller = new LoginWindowController();

}
