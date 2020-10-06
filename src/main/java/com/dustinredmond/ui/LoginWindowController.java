package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.EventService;
import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;

public class LoginWindowController {
    public boolean authenticate(TextField textFieldUser, PasswordField textFieldPass) {
        if (textFieldUser.getText().trim().isEmpty() || textFieldPass.getText().trim().isEmpty()) {
            return false;
        }
        GitHubClient client = GitHubApi.getInstance().getGitHubClient();
        client.setCredentials(textFieldUser.getText(), (textFieldPass.getText()));
        return isAuthenticatedClient(client);
    }

    private boolean isAuthenticatedClient(GitHubClient client) {
        UserService u = new UserService(client);
        try {
            u.getUser(); // throws exception if unable to authenticate
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
