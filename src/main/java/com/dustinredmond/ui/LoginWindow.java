package com.dustinredmond.ui;

import com.dustinredmond.javafx.CustomAlert;
import com.dustinredmond.javafx.PaddedGridPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.prefs.Preferences;


public class LoginWindow {

    public LoginWindow() {
        GridPane grid = new PaddedGridPane(5, 10);
        UI.setTitleContext("Login");

        int rowIndex = 0;

        Preferences prefs = Preferences.userNodeForPackage(LoginWindow.class);

        PasswordField textFieldPass = new PasswordField();
        textFieldPass.setText(prefs.get("ghAccessToken", ""));
        textFieldPass.setPromptText("GitHub personal access token");
        grid.add(new Label("GitHub Access Token:"), 0, rowIndex);
        grid.add(textFieldPass, 1, rowIndex++);

        GridPane.setHgrow(textFieldPass, Priority.ALWAYS);

        CheckBox cbSave = new CheckBox("Save Access Token");
        cbSave.setSelected(!prefs.get("ghAccessToken", "").equals(""));
        grid.add(cbSave, 0, rowIndex++);

        Button buttonLogin = new Button("Login to GitHub");
        buttonLogin.setOnAction(e -> {
            if (controller.authenticate(textFieldPass)) {
                if (cbSave.isSelected()) {
                    prefs.put("ghAccessToken", textFieldPass.getText());
                } else {
                    prefs.remove("ghAccessToken");
                }
                new GistOverviewWindow();
            } else {
                textFieldPass.setText("");
                CustomAlert.showWarning("Unable to authenticate with provided access token.");
            }
        });
        grid.add(buttonLogin, 0, rowIndex);

        UI.getStage().getScene().setRoot(grid);

        grid.requestFocus(); // So user can see first TextField's prompt text
    }

    private static final LoginWindowController controller = new LoginWindowController();

}
