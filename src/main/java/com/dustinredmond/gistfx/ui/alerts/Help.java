package com.dustinredmond.gistfx.ui.alerts;

import com.dustinredmond.gistfx.Main;
import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import com.dustinredmond.gistfx.utils.AppConstants;
import com.dustinredmond.gistfx.utils.Modify;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import javafx.stage.Modality;

import java.io.File;
import java.net.URL;
import java.util.*;

public class Help {

	public static void somethingWrong() {
		String htmlFilePath = Objects.requireNonNull(Main.class.getResource("HelpFiles/IfSomethingWrong.html")).toExternalForm();
		File   htmlFile     = new File(htmlFilePath.replaceFirst("file:", ""));
		String html         = Action.loadTextFile(htmlFile);
		showHelp(html);
	}

	public static void showIntro() {
		File   htmlFile = new File(AppConstants.htmlFilePath, "Intro.html");
		String html     = Action.loadTextFile(htmlFile).replaceFirst("LogoFile", getLogoPath().toString());
		showHelp(html);
	}

	public static void showCreateTokenHelp() {
		File   htmlFile = new File(AppConstants.htmlFilePath, "HelpCreateToken.html");
		String html     = Action.loadTextFile(htmlFile);
		String search   = "LogoFile";
		String replace  = "file:" + AppConstants.getHtmlFilePathWith("GistFXLogo.png");
		html = Modify.string().replace(html, search, replace);
		List<String> searchList = Modify.string().extractList(html, "~~File\\d{1,}~~");
		Collections.sort(searchList);
		List<File>   images             = Arrays.stream(Objects.requireNonNull(new File(AppConstants.getHtmlFilePathWith("HowToToken")).listFiles())).toList();
		List<String> imageAbsolutePaths = new ArrayList<>();
		for (File file : images) {
			imageAbsolutePaths.add("file:" + file.getAbsolutePath());
		}
		Collections.sort(imageAbsolutePaths);
		for (int x = 1; x <= searchList.size(); x++) {
			replace = imageAbsolutePaths.get(x - 1);
			html    = Modify.string().replace(html, searchList.get(x - 1), replace);
		}
		showHelp(html);
	}

	private static String getMain() {
		File   htmlFile = new File(AppConstants.htmlFilePath, "Main.html");
		String html     = Action.loadTextFile(htmlFile).replaceFirst("LogoFile", getLogoPath().toString());

		for (int x = 1; x <= 2; x++) {
			URL    url    = Main.class.getResource("HelpFiles/General/" + x + ".png");
			String search = "~~File" + x + "~~";
			assert url != null;
			String replace = url.toString();
			html = Modify.string().replace(html, search, replace);
		}
		return html;
	}

	public static void mainOverview() {
		showHelp(getMain());
	}

	public static void generalHelp() {
		File   htmlFile = new File(AppConstants.htmlFilePath, "GeneralHelp.html");
		String html     = Action.loadTextFile(htmlFile);
		showHelp(html);
	}

	private static void showHelp(String html) {
		String background = "~background~";
		String color      = "~color~";
		if (LiveSettings.theme.equals(Theme.DARK)) {
			html = Modify.string().replace(html, background, "background-color:#373e43");
			html = Modify.string().replace(html, color, "color:lightgrey");
		}
		else {
			html = Modify.string().replace(html, background, "background-color:#e6e6e6");
			html = Modify.string().replace(html, color, "color:black");
		}
		WebView webView = new WebView();
		webView.getEngine().loadContent(html);
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getButtonTypes().clear();
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		alert.getButtonTypes().add(ButtonType.OK);
		alert.getDialogPane().setContent(webView);
		alert.getDialogPane().setPadding(new Insets(10, 20, 0, 10));
		alert.showAndWait();
	}

	private static URL getLogoPath() {
		return Main.class.getResource("HelpFiles/GistFXLogo.png");
	}
}
