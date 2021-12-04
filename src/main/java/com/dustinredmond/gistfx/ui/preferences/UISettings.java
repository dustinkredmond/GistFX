package com.dustinredmond.gistfx.ui.preferences;

import com.dustinredmond.gistfx.Main;
import com.dustinredmond.gistfx.ui.CodeEditor;
import com.dustinredmond.gistfx.ui.alerts.CustomAlert;
import com.dustinredmond.gistfx.ui.enums.Type;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class UISettings {

	private static final double cbw   = 100;
	private static       Alert  alert = new Alert(Alert.AlertType.NONE);

	private static Label newLabel(String text) {
		Label label = new Label(text);
		label.setPrefWidth(155);
		label.setAlignment(Pos.CENTER_LEFT);
		return label;
	}

	private static VBox newVBox(Node... nodes) {
		VBox vbox = new VBox(nodes);
		vbox.setAlignment(Pos.CENTER);
		vbox.setSpacing(20);
		return vbox;
	}

	private static HBox newHBox(Node... nodes) {
		HBox hbox = new HBox(nodes);
		hbox.setAlignment(Pos.CENTER);
		hbox.setSpacing(20);
		return hbox;
	}

	private static HBox hboxLeft(Node... nodes) {
		HBox hbox = new HBox(nodes);
		hbox.setSpacing(0);
		hbox.setAlignment(Pos.CENTER_LEFT);
		return hbox;
	}

	private static HBox hboxRight(Node... nodes) {
		HBox hbox = new HBox(nodes);
		hbox.setSpacing(0);
		hbox.setAlignment(Pos.CENTER_RIGHT);
		return hbox;
	}

	private static Node theme(Scene callingScene) {
		return Theme.getNode(callingScene);
	}

	private static Node loginScreen() {
		return LoginScreen.getNode();
	}

	private static Node dataSource() {
		return DataSource.getNode();
	}

	private static Node progressBarColor() {
		return ProgressBarColor.getNode();
	}

	private static Node progressBarRandomChoice() {
		return ProgressBarRandom.getNode();
	}

	public static void setStyleSheet(String styleSheet) {
		alert.getDialogPane().getStylesheets().clear();
		alert.getDialogPane().getStylesheets().add(styleSheet);
	}

	public static void showWindow(Scene callingScene) {

		Button btnReset = new Button("Reset Password And Token");
		btnReset.setOnAction(e -> {
			if (CustomAlert.showConfirmation("Are you sure you want to delete your local Token and Password?\n\nYou will be requiured to type in a valid token the next time you launch GistFX.\nYou will also have the option of creating a new password at next login.")) {
				UserPreferences.clearPasswordHash();
				UserPreferences.clearTokenHash();
				CustomAlert.showInfo("Success!", null);
			}
		});
		HBox hboxButtonReset = newHBox(btnReset);
		hboxButtonReset.setAlignment(Pos.CENTER);
		CheckBox chkButtonBar = new CheckBox("Show button bar when form loads");
		chkButtonBar.selectedProperty().addListener((observable, oldValue, newValue) -> UserPreferences.setShowButtonBar(newValue));
		chkButtonBar.setSelected(UserPreferences.getShowButtonBar());
		HBox hboxBBChk   = hboxLeft(chkButtonBar);
		VBox formContent = new VBox(hboxBBChk, progressBarRandomChoice(), progressBarColor(), theme(callingScene), loginScreen(), dataSource(), hboxButtonReset);
		formContent.setPadding(new Insets(10, 10, 10, 10));
		formContent.setSpacing(20);
		formContent.setAlignment(Pos.CENTER);
		alert = new Alert(Alert.AlertType.NONE);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(0, new ButtonType("Close", ButtonBar.ButtonData.LEFT));//For some reason, .LEFT centers the button when there is only one button in the alert
		alert.setTitle("GistFX Setting Options");
		alert.getDialogPane().setContent(formContent);
		alert.getDialogPane().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		alert.showAndWait();
	}

	public enum Theme {

		DARK,
		LIGHT,
		PROGRESS_BAR,
		TEXT_FIELD,
		TEXT_AREA;

		public static String darkCSS;
		public static String lightCSS;
		public static String progressCSS;
		public static String textFieldCSS;
		public static String textAreaCSS;

		public static String get(Theme theme) {
			return switch (theme) {
				case DARK -> "Dark";
				case LIGHT -> "Light";
				case PROGRESS_BAR -> "Progress";
				case TEXT_FIELD -> "TextField";
				case TEXT_AREA -> "TextArea";
			};
		}

		public static Theme get(String theme) {
			return switch (theme) {
				case "Dark" -> DARK;
				case "Light" -> LIGHT;
				case "progress" -> PROGRESS_BAR;
				default -> null;
			};
		}

		public static void init() {
			darkCSS      = Objects.requireNonNull(Main.class.getResource("StyleSheets/Dark.css")).toExternalForm();
			lightCSS     = Objects.requireNonNull(Main.class.getResource("StyleSheets/Light.css")).toExternalForm();
			progressCSS  = Objects.requireNonNull(Main.class.getResource("StyleSheets/ProgressBar.css")).toExternalForm();
			textFieldCSS = Objects.requireNonNull(Main.class.getResource("StyleSheets/TextField.css")).toExternalForm();
			textAreaCSS  = Objects.requireNonNull(Main.class.getResource("StyleSheets/TextArea.css")).toExternalForm();
		}

		public static Node getNode(Scene callingScene) {
			Label                 label     = newLabel("Application Theme");
			ObservableList<Theme> themeList = FXCollections.observableList(Arrays.asList(DARK, LIGHT));
			ChoiceBox<Theme>      choiceBox = new ChoiceBox<>(themeList);
			choiceBox.setPrefWidth(cbw);
			choiceBox.setValue(UserPreferences.getTheme());
			choiceBox.setOnAction(e -> {
				Theme theme = choiceBox.getValue();
				UserPreferences.setTheme(theme);
				LiveSettings.applyUserPreferences();
				callingScene.getStylesheets().clear();
				callingScene.getStylesheets().add(theme.getStyleSheet());
				UISettings.setStyleSheet(theme.getStyleSheet());
				CodeEditor.get().getEditor().setCurrentTheme(theme.equals(LIGHT) ? "vs-light" : "vs-dark");
			});
			return newHBox(hboxLeft(label), hboxRight(choiceBox));
		}

		public String Name(Theme this) {
			return switch (this) {
				case DARK -> "Dark";
				case LIGHT -> "Light";
				case PROGRESS_BAR -> "Progress";
				case TEXT_FIELD -> "TextField";
				case TEXT_AREA -> "TextArea";
			};
		}

		public String getStyleSheet(Theme this) {
			return switch (this) {
				case DARK -> darkCSS;
				case LIGHT -> lightCSS;
				case PROGRESS_BAR -> progressCSS;
				case TEXT_FIELD -> textFieldCSS;
				case TEXT_AREA -> textAreaCSS;
			};
		}
	}

	public enum DataSource {
		LOCAL,
		GITHUB,
		UNKNOWN;

		static HBox      hboxSeconds = null;
		static TextField tfSeconds   = null;

		public static String get(DataSource pref) {
			return switch (pref) {
				case LOCAL -> "local";
				case GITHUB -> "git";
				case UNKNOWN -> "unknown";
			};
		}

		public static DataSource get(String pref) {
			return switch (pref) {
				case "local" -> LOCAL;
				case "git" -> GITHUB;
				case "unknown" -> UNKNOWN;
				default -> null;
			};
		}

		public static VBox getNode() {
			ObservableList<DataSource> list      = FXCollections.observableArrayList(DataSource.LOCAL, DataSource.GITHUB);
			Label                      label     = newLabel("Live Save Location");
			ChoiceBox<DataSource>      choiceBox = new ChoiceBox<>(list);
			choiceBox.setPrefWidth(cbw);
			choiceBox.setOnAction(e -> {
			});
			choiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
				if (!(oldValue == null)) {
					hboxSeconds.setVisible(newValue.equals(DataSource.GITHUB));
					UserPreferences.setLiveLocation(choiceBox.getValue());
					LiveSettings.applyUserPreferences();
					if (newValue.equals(DataSource.GITHUB)) {
						tfSeconds.requestFocus();
					}
				}
			});
			choiceBox.setValue(UserPreferences.getLiveLocation());
			Tooltip.install(choiceBox, new Tooltip("This option determines whether or not the changes that you make to a Gist file are stored locally until you choose to upload them (recommended) OR if they are uploaded to GitHib at regular intervals as you edit the file."));
			Label lblSeconds = new Label("Seconds between uploads");
			tfSeconds = new TextField();
			tfSeconds.setText(String.valueOf(UserPreferences.getLiveInterval()));
			tfSeconds.textProperty().addListener(((observable, oldValue, newValue) -> {
				if (!newValue.equals(oldValue)) {
					String value = newValue.replaceAll("[^0-9]+", "");
					Platform.runLater(() -> tfSeconds.setText(value));
					if (!value.isEmpty()) {
						Integer seconds = Integer.parseInt(value);
						UserPreferences.setLiveInterval(seconds);
					}
				}
			}));
			tfSeconds.setMaxWidth(50);
			HBox hboxChoice = newHBox(hboxLeft(label), hboxRight(choiceBox));
			hboxSeconds = newHBox(lblSeconds, tfSeconds);
			hboxSeconds.setVisible(UserPreferences.getLiveLocation().equals(GITHUB));
			return newVBox(hboxChoice, hboxSeconds);
		}

		public String Name(DataSource this) {
			return switch (this) {
				case LOCAL -> "local";
				case GITHUB -> "git";
				case UNKNOWN -> "unknown";
			};
		}
	}

	public enum LoginScreen {
		STANDARD,
		GRAPHIC,
		PASSWORD_LOGIN,
		TOKEN_LOGIN,
		UNKNOWN;

		public static String get(LoginScreen pref) {
			return switch (pref) {
				case STANDARD -> "standard";
				case GRAPHIC -> "graphic";
				case PASSWORD_LOGIN -> "password.login";
				case TOKEN_LOGIN -> "token.login";
				case UNKNOWN -> "unknown";
			};
		}

		public static LoginScreen get(String pref) {
			return switch (pref) {
				case "standard" -> STANDARD;
				case "graphic" -> GRAPHIC;
				case "password.login" -> PASSWORD_LOGIN;
				case "token.login" -> TOKEN_LOGIN;
				case "unknown" -> UNKNOWN;
				default -> null;
			};
		}

		public static HBox getNode() {
			ObservableList<LoginScreen> list      = FXCollections.observableArrayList(LoginScreen.STANDARD, LoginScreen.GRAPHIC);
			Label                       label     = newLabel("Preferred Login Screen");
			ChoiceBox<LoginScreen>      choiceBox = new ChoiceBox<>(list);
			choiceBox.setPrefWidth(cbw);
			choiceBox.setOnAction(e -> {
				UserPreferences.setLoginScreenChoice(choiceBox.getValue());
				LiveSettings.applyUserPreferences();
			});
			choiceBox.setValue(UserPreferences.getLogonScreenChoice());
			return newHBox(hboxLeft(label), hboxRight(choiceBox));
		}

		public String Name(LoginScreen this) {
			return switch (this) {
				case STANDARD -> "standard";
				case GRAPHIC -> "graphic";
				case PASSWORD_LOGIN -> "password.login";
				case TOKEN_LOGIN -> "token.login";
				case UNKNOWN -> "unknown";
			};
		}

	}

	public enum ProgressBarColor {

		GREEN,
		YELLOW,
		ORANGE,
		RED,
		BLUE,
		CYAN,
		HOTPINK,
		OCEAN,
		BLACK;

		public static final ObservableList<ProgressBarColor> colors    = FXCollections.observableArrayList(
				GREEN,
				YELLOW,
				ORANGE,
				RED,
				BLUE,
				CYAN,
				HOTPINK,
				OCEAN);
		public static       ChoiceBox<ProgressBarColor>      choiceBox = new ChoiceBox<>();

		public static ProgressBarColor get(String color) {
			return switch (color) {
				case "green-bar" -> GREEN;
				case "yellow-bar" -> YELLOW;
				case "orange-bar" -> ORANGE;
				case "red-bar" -> RED;
				case "blue-bar" -> BLUE;
				case "cyan-bar" -> CYAN;
				case "hotpink-bar" -> HOTPINK;
				case "ocean-bar" -> OCEAN;
				default -> null;
			};
		}

		public static HBox getNode() {
			Collections.sort(colors);
			Label label = newLabel("Preferred Progressbar Color");
			choiceBox = new ChoiceBox<>(colors);
			choiceBox.setValue(UserPreferences.getProgressBarColor());
			choiceBox.setPrefWidth(cbw);
			choiceBox.setOnAction(e -> {
				UserPreferences.setProgressBarColor(choiceBox.getValue());
				LiveSettings.applyUserPreferences();
			});
			choiceBox.visibleProperty().bind(ProgressBarRandom.checkBox.selectedProperty().not());
			label.visibleProperty().bind(ProgressBarRandom.checkBox.selectedProperty().not());
			return newHBox(hboxLeft(label), hboxRight(choiceBox));
		}

		public String Name(ProgressBarColor this) {
			return switch (this) {
				case GREEN -> "green-bar";
				case YELLOW -> "yellow-bar";
				case ORANGE -> "orange-bar";
				case RED -> "red-bar";
				case BLUE -> "blue-bar";
				case CYAN -> "cyan-bar";
				case HOTPINK -> "hotpink-bar";
				case OCEAN -> "ocean-bar";
				case BLACK -> "black-bar";
			};
		}

		public String getStyleClass(ProgressBarColor this) {
			return switch (this) {
				case GREEN -> "green-bar";
				case YELLOW -> "yellow-bar";
				case ORANGE -> "orange-bar";
				case RED -> "red-bar";
				case BLUE -> "blue-bar";
				case CYAN -> "cyan-bar";
				case HOTPINK -> "hotpink-bar";
				case OCEAN -> "ocean-bar";
				case BLACK -> "black-bar";
			};
		}
	}

	public enum ProgressBarRandom {
		RANDOM,
		STANDARD;

		public static final CheckBox checkBox = new CheckBox();

		public static String get(ProgressBarRandom pref) {
			return switch (pref) {
				case RANDOM -> "random";
				case STANDARD -> "standard";
			};
		}

		public static Type get(String pref) {
			return switch (pref) {
				case "random" -> Type.RANDOM;
				case "standard" -> Type.STANDARD;
				default -> null;
			};

		}

		public static Node getNode() {
			checkBox.setText("Use Randomized Color Progress Bars");
			checkBox.setSelected(UserPreferences.getRandomProgressBarChoice().equals(Type.RANDOM));
			checkBox.setOnAction(e -> {
				UserPreferences.setUseRandomColorProgressBars(checkBox.isSelected() ? ProgressBarRandom.RANDOM : ProgressBarRandom.STANDARD);
				LiveSettings.applyUserPreferences();
			});
			return hboxLeft(checkBox);
		}

		public String Name(ProgressBarRandom this) {
			return switch (this) {
				case RANDOM -> "random";
				case STANDARD -> "standard";
			};
		}
	}
}
