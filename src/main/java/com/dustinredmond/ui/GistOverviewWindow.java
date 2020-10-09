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
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
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
        buttonEdit.setOnAction(e -> controller.editGist(table));
        buttonDelete.setOnAction(e -> controller.deleteGist(table));
        GridPane.setVgrow(table, Priority.ALWAYS);
        GridPane.setHgrow(table, Priority.ALWAYS);
        grid.add(table, 0, rowIndex);
    }

    private void displayGistFileList(Gist gist) {
        Stage stage = new Stage();

        String gistDesc = StringUtils.truncate(gist.getDescription(), 45) + "...";
        stage.setTitle(String.format("%s - %s - %s", UI.APP_TITLE, gistDesc, "Select a Gist File"));
        PaddedGridPane grid = new PaddedGridPane(5, 10);

        ListView<GistFile> listView = getGistFileListView(gist);
        grid.add(listView, 0, 0);
        GridPane.setHgrow(listView, Priority.ALWAYS);
        GridPane.setVgrow(listView, Priority.ALWAYS);

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

        MenuItem miAdd = new MenuItem("Add Gist");
        miAdd.setOnAction(e -> controller.createGist(table));
        MenuItem miEdit = new MenuItem("Edit Gist");
        miEdit.setOnAction(e -> controller.editGist(table));
        MenuItem miDelete = new MenuItem("Delete Gist");
        miDelete.setOnAction(e -> controller.deleteGist(table));
        cm.getItems().addAll(miAdd, miEdit, miDelete);
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

        return listView;
//        TableView<GistFile> table = new TableView<>();
//
//        TableColumn<GistFile, String> columnTitle = new TableColumn<>("File");
//        columnTitle.setCellValueFactory(f -> new SimpleObjectProperty<>(f.getValue().getFilename()));
//        table.getColumns().add(columnTitle);
//
//        TableColumn<GistFile, String> columnUrl = new TableColumn<>("URL");
//        columnUrl.setCellValueFactory(f ->
//                new SimpleStringProperty(
//                        StringUtils.truncate(f.getValue().getRawUrl(), 60)));
//        table.getColumns().add(columnUrl);
//        gist.getFiles().forEach((fileName, gistFile) -> table.getItems().add(gistFile));
//
//        table.setOnMouseClicked(click -> {
//            if (MouseButton.PRIMARY.equals(click.getButton()) && click.getClickCount() == 2
//                    && !table.getSelectionModel().isEmpty()) {
//                GistFile file = table.getSelectionModel().getSelectedItem();
//                GistEditor.showEdit(gist, file);
//            }
//        });

//        return table;
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
