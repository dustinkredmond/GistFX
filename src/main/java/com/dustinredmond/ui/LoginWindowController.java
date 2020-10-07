package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import javafx.scene.control.PasswordField;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;


public class LoginWindowController {
    public boolean authenticate(PasswordField textFieldPass) {
        if (textFieldPass.getText().trim().isEmpty()) {
            return false;
        }
        GitHubClient client = GitHubApi.getInstance().getGitHubClient();
        client.setOAuth2Token(textFieldPass.getText());
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
