package com.dustinredmond.ui;

import com.dustinredmond.github.GitHubApi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.eclipse.egit.github.core.Gist;

public class GistOverviewWindow {

    public GistOverviewWindow() {

        // TODO this will be the main view, an overview of all gists,
        //  Things to implement:
        //    1. Sorting/Filtering
        //    2. Export as file
        //    3. CRUD
        //    4. Editor with syntax based on language

        TableView<Gist> table = new TableView<>();

        TableColumn<Gist, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().add(descriptionColumn);

        ObservableList<Gist> gistList = FXCollections.observableArrayList();
        gistList.addAll(GitHubApi.getInstance().getGists());

        table.setItems(gistList);

        UI.getStage().getScene().setRoot(table);
    }
}
