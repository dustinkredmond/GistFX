package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import com.dustinredmond.javafx.CustomAlert;
import com.dustinredmond.javafx.PaddedGridPane;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;

public class GistEditor {

    public static void showEdit(Gist gist, GistFile gistFile) {
        // NOTE gistFile's getContent will return null, get
        // instead from GistService

        GistService service = new GistService(GitHubApi.getInstance().getGitHubClient());

        String content;
        try {
            content = service.getGist(gist.getId()).getFiles().get(gistFile.getFilename()).getContent();
        } catch (IOException ignored) {
            content = "";
        }

        Stage stage = new Stage();
        stage.setTitle(String.format("%s - %s", UI.APP_TITLE, gistFile.getFilename()));
        PaddedGridPane grid = new PaddedGridPane(5, 10);

        TextArea taCode = new TextArea(content);
        grid.add(taCode, 0, 1);
        GridPane.setVgrow(taCode, Priority.ALWAYS);
        GridPane.setHgrow(taCode, Priority.ALWAYS);

        Button buttonSubmit = new Button("Save Changes");
        buttonSubmit.setOnAction(e -> {
            gistFile.setContent(taCode.getText());
            gist.getFiles().put(gistFile.getFilename(), gistFile);
            saveUpdatedGistFile(gist, stage);
        });
        grid.add(buttonSubmit, 0, 2);

        stage.setScene(new Scene(grid));
        stage.show();
    }

    private static void saveUpdatedGistFile(Gist gist, Stage parentStage) {
        GistService service = GitHubApi.getInstance().getGistService();
        try {
            service.updateGist(gist);
            final String promptContinue = "Gist successfully saved. Would you like to continue" +
                    " editing?";
            if (!CustomAlert.showConfirmation(promptContinue)) {
                parentStage.hide();
            }
        } catch (IOException e) {
            CustomAlert.showExceptionDialog(e, "Error occurred when attempting to save changes.");
        }
    }


}
