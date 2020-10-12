package com.dustinredmond.ui;

import com.dustinredmond.javafx.CustomStage;
import com.dustinredmond.javafx.PaddedGridPane;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

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
            Stage stage = new CustomStage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(UI.APP_TITLE + " - About this program");

            PaddedGridPane grid = new PaddedGridPane(5, 30);
            stage.setScene(new Scene(grid));

            final int year = LocalDate.now().getYear();
            final String version = getClass().getPackage().getImplementationVersion();
            final Label text = new Label(UI.APP_TITLE+"\nVersion: " + version + "\n");
            final TextArea taLicense = new TextArea(
                    "Copyright \u00A9 " + year + " Dustin K. Redmond <dredmond@gaports.com>\n\n" +
                            "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                            "of this software and associated documentation files (the \"Software\"), to deal\n" +
                            "in the Software without restriction, including without limitation the rights\n" +
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                            "copies of the Software, and to permit persons to whom the Software is\n" +
                            "furnished to do so, subject to the following conditions:\n" +
                            "\n" +
                            "The above copyright notice and this permission notice shall be included in all\n" +
                            "copies or substantial portions of the Software.\n" +
                            "\n" +
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                            "SOFTWARE." +
                            "\n" +
                            "\n" +
                            "Application icons provided by https://icons8.com");
            taLicense.setEditable(false);
            VBox vBox = new VBox(5, text, taLicense);
            grid.getChildren().add(vBox);
            stage.show();
        });
    }

}
