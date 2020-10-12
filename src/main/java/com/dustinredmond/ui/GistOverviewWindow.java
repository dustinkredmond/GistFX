package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import com.dustinredmond.javafx.CustomAlert;
import com.dustinredmond.javafx.PaddedGridPane;
import com.dustinredmond.utils.StringUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import java.util.LinkedHashMap;
import java.util.Objects;

public class GistOverviewWindow {

    public GistOverviewWindow() {
        UI.setTitleContext(String.format("Gist Overview (%s)",
                Objects.requireNonNull(GitHubApi.getCurrentUser()).getLogin()));

        BorderPane root = new BorderPane();
        UI.getStage().getScene().setRoot(root);
        UI.getStage().setMaximized(true);
        PaddedGridPane grid = new PaddedGridPane(5, 10);
        root.setCenter(grid);

        int rowIndex = 0;

        ButtonBar buttonBar = new ButtonBar();
        Button buttonAdd = new Button("Add Gist");
        Button buttonEdit = new Button("Edit Gist");
        Button buttonDelete = new Button("Delete Gist");
        buttonBar.getButtons().addAll(buttonAdd, buttonEdit, buttonDelete);
        grid.add(buttonBar, 0, rowIndex++, 2, 1);

        TableView<Gist> table = getGistTableView();
        buttonAdd.setOnAction(e -> controller.createGist(table));
        buttonEdit.setOnAction(e -> controller.editGist(table));
        buttonDelete.setOnAction(e -> controller.deleteGist(table));
        GridPane.setVgrow(table, Priority.ALWAYS);
        GridPane.setHgrow(table, Priority.ALWAYS);

        grid.add(table, 0, rowIndex);
    }

    private void displayGistFileList(Gist gist) {
        Stage stage = new Stage();

        String gistDesc = StringUtils.truncate(gist.getDescription(), 30) + "...";
        stage.setTitle(String.format("%s - %s - %s", UI.APP_TITLE, gistDesc, "Select a Gist File"));
        PaddedGridPane grid = new PaddedGridPane(5, 10);

        Button buttonAddFile = new Button("Add File");
        Button buttonEditFile = new Button("Edit File");
        Button buttonDeleteFile = new Button("Delete File");
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(buttonAddFile, buttonEditFile, buttonDeleteFile);
        grid.add(buttonBar, 0, 0);

        ListView<GistFile> listView = getGistFileListView(gist);
        grid.add(listView, 0, 1);
        GridPane.setHgrow(listView, Priority.ALWAYS);
        GridPane.setVgrow(listView, Priority.ALWAYS);

        buttonAddFile.setOnAction(e -> controller.createGistFile(listView, gist));
        buttonEditFile.setOnAction(e -> {
            if (!listView.getSelectionModel().isEmpty()) {
                GistEditor.showEdit(gist, listView.getSelectionModel().getSelectedItem());
            } else {
                CustomAlert.showWarning("Please select a Gist file first.");
            }
        });
        buttonDeleteFile.setOnAction(e -> controller.deleteGistFile(listView, gist));

        stage.setScene(new Scene(grid, 600, 200));
        stage.centerOnScreen();
        stage.show();
    }

    private TableView<Gist> getGistTableView() {
        TableView<Gist> table = new TableView<>();
        gistPropertyMap().forEach((title,property) -> {
            TableColumn<Gist, String> column = new TableColumn<>(title);
            column.setCellValueFactory(new PropertyValueFactory<>(property));
            table.getColumns().add(column);
        });

        TableColumn<Gist, Boolean> column = new TableColumn<>("Is Public?");
        column.setCellValueFactory(gist -> new SimpleBooleanProperty(gist.getValue().isPublic()));
        column.setCellFactory(cell -> new CheckBoxTableCell<>());
        table.getColumns().add(column);

        ObservableList<Gist> gistList = FXCollections.observableArrayList();
        gistList.addAll(GitHubApi.getInstance().getGists());
        table.setItems(gistList);

        table.setOnMouseClicked(mouseClick -> {
            if (MouseButton.PRIMARY.equals(mouseClick.getButton())
                    && mouseClick.getClickCount() == 2
                    && !table.getSelectionModel().isEmpty()) {
                Gist gist = table.getSelectionModel().getSelectedItem();
                displayGistFileList(gist);
            }
        });
        
        // Don't show the context menu if no table item is selected
        // or if the context menu is already open
        table.setOnContextMenuRequested(e -> {
            if (table.getSelectionModel().isEmpty() || table.getContextMenu().isShowing()) {
                e.consume();
            }
        });
        
        table.setContextMenu(getGistTableViewContextMenu(table));

        return table;
    }

    private ContextMenu getGistTableViewContextMenu(TableView<Gist> table) {
        ContextMenu cm = new ContextMenu();

        MenuItem miCopy = new MenuItem("Copy Gist URL");
        miCopy.setOnAction(e -> controller.copyUrl(table));
        MenuItem miBrowse = new MenuItem("View on GitHub");
        miBrowse.setOnAction(e -> controller.browseGistUrl(table));

        MenuItem miAdd = new MenuItem("Add Gist");
        miAdd.setOnAction(e -> controller.createGist(table));
        MenuItem miEdit = new MenuItem("Edit Gist");
        miEdit.setOnAction(e -> controller.editGist(table));
        MenuItem miDelete = new MenuItem("Delete Gist");
        miDelete.setOnAction(e -> controller.deleteGist(table));
        cm.getItems().addAll(miCopy, miBrowse,
                new SeparatorMenuItem(), miAdd, miEdit, miDelete);
        return cm;
    }

    private ListView<GistFile> getGistFileListView(Gist gist) {

        ListView<GistFile> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setCellFactory(param -> new ListCell<GistFile>() {
            @Override
            protected void updateItem(GistFile gistFile, boolean empty) {
                super.updateItem(gistFile, empty);
                if (empty || gistFile == null || gistFile.getFilename() == null) {
                    setText(null);
                } else {
                    setText(gistFile.getFilename());
                }
            }


        });

        gist.getFiles().forEach((fileName, gistFile) -> listView.getItems().add(gistFile));

        listView.setOnMouseClicked(m -> {
            if (MouseButton.PRIMARY.equals(m.getButton()) && m.getClickCount() == 2
                    && !listView.getSelectionModel().isEmpty()) {
                GistFile file = listView.getSelectionModel().getSelectedItem();
                GistEditor.showEdit(gist, file);
            }
        });

        listView.setContextMenu(getGistFileListViewContextMenu(listView, gist));

        return listView;
    }

    private ContextMenu getGistFileListViewContextMenu(ListView<GistFile> listView, Gist gist) {
        ContextMenu cm = new ContextMenu();

        MenuItem miAdd = new MenuItem("Add Gist file");
        miAdd.setOnAction(e -> controller.createGistFile(listView, gist));
        MenuItem miEdit = new MenuItem("Edit Gist file");
        miEdit.setOnAction(e -> {
            if (listView.getSelectionModel().isEmpty()) {
                return;
            }
            GistEditor.showEdit(gist, listView.getSelectionModel().getSelectedItem());
        });

        cm.getItems().addAll(miAdd, miEdit);
        miAdd.setOnAction(e -> controller.createGistFile(listView, gist));

        return cm;
    }

    private static LinkedHashMap<String,String> gistPropertyMap() {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Description", "description");
        map.put("Created", "createdAt");
        map.put("Updated", "updatedAt");
        map.put("Comments", "comments");
        return map;
    }

    private static final GistOverviewWindowController controller = new GistOverviewWindowController();
}
