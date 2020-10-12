package com.dustinredmond.ui;

import com.dustinredmond.javafx.CustomAlert;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class CustomMenuBar extends MenuBar {

    public CustomMenuBar() {
        Menu menuFile = new Menu("File");
        this.getMenus().add(menuFile);

        MenuItem miExit = new MenuItem("Exit program");
        menuFile.getItems().add(miExit);
        miExit.setOnAction(e -> Platform.exit());

        Menu menuAbout = new Menu("About");
        this.getMenus().add(menuAbout);

        MenuItem menuItemAbout = new MenuItem("About this program");
        menuAbout.getItems().add(menuItemAbout);
        menuItemAbout.setOnAction(e -> {
            CustomAlert.showInfo(UI.APP_TITLE,
                    "In development...");
        });
    }

}
