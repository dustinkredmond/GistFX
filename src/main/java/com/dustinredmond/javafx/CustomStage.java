package com.dustinredmond.javafx;

import com.dustinredmond.ui.UI;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class CustomStage extends Stage {

    public CustomStage() {
        this.getIcons().add(new Image(UI.class.getResourceAsStream(UI.APP_ICON)));
    }

}
