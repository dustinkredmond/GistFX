package com.dustinredmond.gistfx;


import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.data.json.Json;
import com.dustinredmond.gistfx.ui.LoginWindow;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import com.dustinredmond.gistfx.ui.preferences.UserPreferences;
import com.dustinredmond.gistfx.utils.Scener;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Scanner;

public class Main extends Application {
    public static void main(String[] args) {
        boolean stopFlag = false;
        for (String arg : args) {
            if (arg.toLowerCase(Locale.ROOT).startsWith("newdatabase")) {
                Action.deleteDatabaseFile();
                System.out.println("Database file has been reset");
                stopFlag = true;
            }
            if (arg.toLowerCase(Locale.ROOT).startsWith("clearcreds")) {
                UserPreferences.clearTokenHash();
                UserPreferences.clearPasswordHash();
                System.out.println("Credentials Cleared");
                stopFlag = true;
            }
            if (arg.toLowerCase(Locale.ROOT).startsWith("factory")) {
                Action.setDatabaseConnection();
                Scanner scanner = new Scanner(new InputStreamReader(System.in));
                System.out.println("This action will reset the local database, which will delete any unsaved changes in your Gist files, and set all preferences back to default.\n\nAre you sure (Y/N)? ");
                String input = scanner.nextLine();
                if (input.toLowerCase().startsWith("y")) {
                    factoryReset();
                    System.out.println("\nWould you also like to reset the custom names you have given to your Gists (Y/N)? ");
                    input = scanner.nextLine();
                    if (input.toLowerCase().startsWith("y")) {
                        Json.reset();
                    }
                }
                stopFlag = true;
            }
        }
        if (stopFlag) System.exit(100);
        launch(args);
    }

    private static void factoryReset() {
        Action.deleteDatabaseFile();
        UserPreferences.resetPreferences();
        System.out.println("Factory Reset Complete");
    }

    @Override public void start(Stage primaryStage) {
        Theme.init();
        LiveSettings.applyUserPreferences();
        Action.setDatabaseConnection();
        Scener.start(primaryStage, 100);
        setUserAgentStylesheet(STYLESHEET_MODENA);
        new LoginWindow();
    }

}
