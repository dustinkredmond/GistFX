package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import com.dustinredmond.javafx.CustomAlert;
import com.dustinredmond.javafx.PaddedGridPane;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;

public class GistOverviewWindowController {

    public void createGist(TableView<Gist> table) {

    }

    public void editGist(TableView<Gist> table) {
        if (table.getSelectionModel().isEmpty()) {
            CustomAlert.showWarning("Please select a Gist " +
                    "from the table first");
            return;
        }
            showEditGistForm(table);
    }

    private void showEditGistForm(TableView<Gist> table) {
        Stage stage = new Stage();
        stage.setTitle(String.format("%s - Edit Gist", UI.APP_TITLE));
        PaddedGridPane grid = new PaddedGridPane(5, 10);
        stage.setScene(new Scene(grid));

        Gist gist = table.getSelectionModel().getSelectedItem();

        TextField tfId = new TextField(gist.getId());
        tfId.setEditable(false);
        grid.add(new Label("ID:"), 0, 0);
        grid.add(tfId, 1, 0);

        TextArea taDescription = new TextArea(gist.getDescription());
        grid.add(new Label("Gist Description"), 0, 1);
        grid.add(taDescription, 0, 2, 2, 1);

        Button buttonSave = new Button("Save Changes");
        buttonSave.setOnAction(e -> {
            final String description = taDescription.getText();
            gist.setDescription(description);
            table.refresh();
            this.updateGist(gist, description);
            stage.hide();
        });
        grid.add(buttonSave, 0, 3);

        stage.show();
    }

    private void updateGist(Gist gist, String description) {
        GistService service = GitHubApi.getInstance().getGistService();
        try {
            Gist githubGist = service.getGist(gist.getId());
            githubGist.setDescription(description);
            service.updateGist(githubGist);
        } catch (IOException e) {
            CustomAlert.showExceptionDialog(e, "Unable to update description.");
        }
    }

    public void deleteGist(TableView<Gist> table) {

        Gist gist = table.getSelectionModel().getSelectedItem();

        final String prompt = String.format("Are you sure you wish to remove" +
                "the following Gist from GitHub?\n\nID: %s\nDescription: %s",
                gist.getId(), gist.getDescription());
        if (CustomAlert.showConfirmation(prompt)) {
            try {
                GitHubApi.getInstance().getGistService().deleteGist(gist.getId());
                table.getItems().remove(gist);
            } catch (IOException e) {
                CustomAlert.showExceptionDialog(e, "Error deleting Gist");
            }
        }
    }
}
