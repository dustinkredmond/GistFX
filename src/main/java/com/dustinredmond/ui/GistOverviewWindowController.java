package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import com.dustinredmond.javafx.CustomAlert;
import com.dustinredmond.javafx.CustomStage;
import com.dustinredmond.javafx.PaddedGridPane;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

public class GistOverviewWindowController {

    public void createGist(TableView<Gist> table) {
        showAddGistForm(table);
    }

    private void showAddGistForm(TableView<Gist> table) {
        Stage stage = new CustomStage();
        PaddedGridPane grid = new PaddedGridPane(5, 10);
        stage.setTitle(UI.APP_TITLE + "Create Gist");
        stage.setScene(new Scene(grid));

        CheckBox cbPublic = new CheckBox();
        cbPublic.setText("Is Public?");
        grid.add(cbPublic, 0, 0);

        TextField tfDescription = new TextField();
        grid.add(new Label("Gist Description:"), 0, 1);
        grid.add(tfDescription, 1, 1);

        TextField tfFileName = new TextField("someFile.txt");
        grid.add(new Label("File Name:"), 0, 2);
        grid.add(tfFileName, 1, 2);

        TextArea taContents = new TextArea("enter some code...");
        grid.add(new Label("Contents:"), 0, 3);
        grid.add(taContents, 0, 4, 2, 1);

        Button buttonAdd = new Button("Create Gist");
        buttonAdd.setOnAction(e -> {
            if (tfDescription.getText().trim().isEmpty()
                    || tfFileName.getText().trim().isEmpty()
                    || taContents.getText().trim().isEmpty()) {
                CustomAlert.showWarning("All fields are required.");
                return;
            }

            try {
                // Must add a file to create a gist
                GistFile file = new GistFile();
                file.setFilename(tfFileName.getText());
                file.setContent(taContents.getText());
                Gist gist = new Gist();
                gist.setPublic(cbPublic.isSelected());
                gist.setDescription(tfDescription.getText().trim());
                gist.setFiles(Collections.singletonMap(file.getFilename(), file));

                GistService service = GitHubApi.getInstance().getGistService();
                Gist createdGist = service.createGist(gist);
                table.getItems().add(createdGist);
                stage.hide();
            } catch (IOException ex) {
                CustomAlert.showExceptionDialog(ex, "Unable to create Gist");
            }
        });
        grid.add(buttonAdd, 0, 5);

        stage.show();

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
        Stage stage = new CustomStage();
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

    public void createGistFile(ListView<GistFile> listView, Gist gist) {
        Stage stage = new CustomStage();
        stage.setTitle(UI.APP_TITLE + " - Create Gist file");
        PaddedGridPane grid = new PaddedGridPane(5, 10);
        stage.setScene(new Scene(grid));

        TextField tfName = new TextField("sample.txt");
        grid.add(new Label("File Name:"), 0, 0);
        grid.add(tfName, 1, 0);

        TextArea taContents = new TextArea("your contents here...");
        grid.add(taContents, 0, 1, 2, 1);

        Button buttonCreateFile = new Button("Create File");

        buttonCreateFile.setOnAction(e -> {
            if (tfName.getText().trim().isEmpty() || taContents.getText().trim().isEmpty()) {
                CustomAlert.showWarning("You must enter a file name and contents.");
                return;
            }

            GistFile gistFile = new GistFile();
            gistFile.setFilename(tfName.getText());
            gistFile.setContent(taContents.getText());

            try {
                GistService service = GitHubApi.getInstance().getGistService();
                Gist oldGist = service.getGist(gist.getId());
                oldGist.getFiles().put(gistFile.getFilename(), gistFile);
                service.updateGist(oldGist);
                listView.getItems().add(gistFile);
                stage.hide();
            } catch (IOException ex) {
                CustomAlert.showExceptionDialog(ex, "Unable to create file.");
            }
        });

        grid.add(buttonCreateFile, 0, 3);
        stage.show();
    }

    public void deleteGistFile(ListView<GistFile> listView, Gist gist) {
        final String prompt = "Deleting a Gist File is not currently " +
                "available from the client. Would you like to open the Gist on GitHub?";
        if (CustomAlert.showConfirmation(prompt)) {
            try {
                Desktop.getDesktop().browse(new URI(gist.getHtmlUrl()));
            } catch (IOException | URISyntaxException e) {
                CustomAlert.showExceptionDialog(e, "Unable to open GitHub " +
                        "in a web browser.");
            }
        }
    }

    public void copyUrl(TableView<Gist> table) {
        if (!table.getSelectionModel().isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(table.getSelectionModel().getSelectedItem().getHtmlUrl());
            clipboard.setContent(content);
        }
    }

    public void browseGistUrl(TableView<Gist> table) {
        if (!table.getSelectionModel().isEmpty()) {
            Gist gist = table.getSelectionModel().getSelectedItem();
            try {
                URI uri = new URI(gist.getHtmlUrl());
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException | IOException e) {
                CustomAlert.showExceptionDialog(e, "Unable to browse to: " + gist.getHtmlUrl());
            }
        }
    }
}
