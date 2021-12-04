package com.dustinredmond.gistfx.ui.alerts;

import com.dustinredmond.gistfx.ui.enums.Response;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import com.dustinredmond.gistfx.utils.Scener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomAlert {

	private static String iconPath;
	private static String applicationTitle = "";

	private static TextField newTextField(String text, String prompt) {
		TextField tf = new TextField(text);
		tf.setPromptText(prompt);
		tf.getStylesheets().add(Theme.TEXT_FIELD.getStyleSheet());
		return tf;
	}

	private static Alert getAlert(Alert.AlertType type, String header, Node content) {
		Alert alert = new Alert(type);
		alert.setTitle(applicationTitle);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setHeaderText(header);
		alert.getDialogPane().setContent(content);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		return alert;
	}

	private static Alert getAlert(Alert.AlertType type, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(applicationTitle);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		return alert;
	}

	public static void showInfo(String headerText, String contentText, Window owner) {
		Alert alert = getAlert(Alert.AlertType.INFORMATION, headerText, contentText);
		alert.initOwner(owner);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		addDialogIconTo(alert, true);
		alert.showAndWait();
	}

	public static boolean showInfoResponse(String contentText, Window owner) {
		Alert alert = getAlert(Alert.AlertType.INFORMATION, "", contentText);
		alert.initOwner(owner);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		addDialogIconTo(alert, true);
		alert.showAndWait();
		return true;
	}

	public static void showInfo(String headerText, Node content, Window owner) {
		Alert alert = getAlert(Alert.AlertType.INFORMATION, headerText, content);
		alert.initOwner(owner);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		addDialogIconTo(alert, true);
		alert.showAndWait();
	}

	public static void showRequireOK(String headerText, String content, Window owner) {
		Alert alert = getAlert(Alert.AlertType.INFORMATION, headerText, content);
		alert.initOwner(owner);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		addDialogIconTo(alert, true);
		alert.showAndWait();
	}

	public static void showInfo(String contentText, Window owner) {
		showInfo("", contentText, owner);
	}

	public static Boolean showPasswordAlert() {
		PasswordField pf1       = new PasswordField();
		PasswordField pf2       = new PasswordField();
		Button        btnOK     = new Button("OK");
		Button        btnCancel = new Button("Cancel");
		Label         label1    = new Label("Set password to protect access token");
		Label         lblPW1    = new Label("Password");
		Label         lblPW2    = new Label("Confirm");
		GridPane      layout    = new GridPane();
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.setVgap(5);
		layout.setHgap(5);
		layout.add(pf1, 1, 1);
		layout.add(pf2, 1, 2);
		layout.add(btnOK, 1, 3);
		layout.add(label1, 1, 0);
		layout.add(lblPW1, 0, 1);
		layout.add(lblPW2, 0, 2);
		btnOK.setOnAction(e -> {
			String pw1 = pf1.getText();
			String pw2 = pf2.getText();
			if (!pw1.equals(pw2)) {
				Platform.runLater(() -> {
					pf2.clear();
					pf1.clear();
					pf1.requestFocus();
				});
			}
		});
		Integer sceneId = Scener.addScene(layout);
		Scener.initModality(sceneId, Modality.APPLICATION_MODAL);
		Scener.setWidthHeight(sceneId, 250, 150);
		Scener.setTitleContext(sceneId, applicationTitle);
		Scener.showAndWait(sceneId);
		return pf1.getText().equals(pf2.getText());
	}

	public static void showWarning(String headerText, String contentText) {
		Platform.runLater(() -> {
			Alert alert = getAlert(Alert.AlertType.ERROR, headerText, contentText);
			addDialogIconTo(alert, false);
			alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
			alert.showAndWait();
		});
	}

	public static void showWarning(String contentText) {
		showWarning("", contentText);
	}

	public static Response showConfirmationResponse(Node content) {
		return showConfirmationResponse("", content);
	}

	public static Response showConfirmationResponse(String headerText, Node content) {
		Alert      alert   = getAlert(Alert.AlertType.NONE, headerText, content);
		ButtonType proceed = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancel  = new ButtonType("Cancel", ButtonBar.ButtonData.OK_DONE);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().setAll(cancel, proceed);
		alert.getDialogPane().setPadding(new Insets(10, 20, 0, 10));
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Button btnProceed = (Button) alert.getDialogPane().lookupButton(proceed);
		Button btnCancel  = (Button) alert.getDialogPane().lookupButton(cancel);
		btnProceed.setDefaultButton(false);
		btnCancel.setDefaultButton(true);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent()) {
			if (result.get().equals(proceed)) {
				return Response.PROCEED;
			}
		}
		return Response.CANCELED;
	}

	public static Response showExitConfirmationResponse(String message) {
		Alert      alert  = getAlert(Alert.AlertType.WARNING, "", message);
		ButtonType save   = new ButtonType("Save To GitHub", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.OK_DONE);
		ButtonType exit   = new ButtonType("Exit Without Saving", ButtonBar.ButtonData.OK_DONE);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().setAll(cancel, save, exit);
		alert.getDialogPane().setPadding(new Insets(10, 20, 0, 10));
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Button btnProceed = (Button) alert.getDialogPane().lookupButton(save);
		Button btnCancel  = (Button) alert.getDialogPane().lookupButton(cancel);
		Button btnExit    = (Button) alert.getDialogPane().lookupButton(exit);
		btnProceed.setDefaultButton(false);
		btnCancel.setDefaultButton(false);
		btnExit.setDefaultButton(true);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent()) {
			if (result.get().equals(save)) {
				return Response.SAVE;
			}
			else if (result.get().equals(exit)) {
				return Response.EXIT;
			}
		}
		return Response.CANCELED;
	}

	public static Boolean showConfirmation(String headerText, String contentText) {
		Alert alert = getAlert(Alert.AlertType.CONFIRMATION, headerText, contentText);
		addDialogIconTo(alert, false);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get().equals(ButtonType.YES);
	}

	public static Response showHardConfirmation(String headerText, String contentText) {
		TextField tfYes      = newTextField("", "Type YES to confirm");
		Label     lblMessage = new Label(contentText);
		lblMessage.setWrapText(true);
		VBox  content = new VBox(lblMessage, tfYes);
		Alert alert   = getAlert(Alert.AlertType.CONFIRMATION, headerText, content);
		addDialogIconTo(alert, false);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.CANCEL);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get().equals(ButtonType.YES)) {
			if (tfYes.getText().equals("YES")) {
				return Response.YES;
			}
			else {
				showInfo("You must type YES in the text field before clicking Yes.", null);
				return Response.MISTAKE;
			}
		}
		return Response.CANCELED;
	}

	public static String showChangeGistNameAlert(String currentName) {
		String message    = "Renaming Gist: " + currentName;
		Label  lblMessage = new Label(message);
		lblMessage.setWrapText(true);
		lblMessage.setPrefWidth(300);
		lblMessage.setPrefHeight(60);
		Label     lblBlank       = new Label(" ");
		Label     lblNewGistName = new Label("New Name:");
		TextField tfGistName     = newTextField(currentName, "");
		tfGistName.setPrefWidth(165);
		tfGistName.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty()) tfGistName.setText(currentName);
		});
		HBox hbox = new HBox(lblNewGistName, tfGistName);
		hbox.setSpacing(10);
		hbox.setPadding(new Insets(10, 10, 10, 10));
		VBox vbox = new VBox(lblMessage, lblBlank, hbox);
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		Alert      alert   = getAlert(Alert.AlertType.NONE, "", vbox);
		ButtonType proceed = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(proceed, ButtonType.CANCEL);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType> option = alert.showAndWait();
		if (option.isPresent() && option.get().equals(ButtonType.CANCEL)) {
			return "";
		}
		return tfGistName.getText();
	}

	public static String showChangeGistDescriptionAlert(String currentDescription) {
		String message    = "Gist Description";
		Label  lblMessage = new Label(message);
		lblMessage.setWrapText(true);
		lblMessage.setPrefWidth(100);
		lblMessage.setPrefHeight(35);
		Label    lblBlank          = new Label(" ");
		Label    lblNewGistName    = new Label("New Name:");
		TextArea taGistDescription = new TextArea(currentDescription);
		taGistDescription.setWrapText(true);
		taGistDescription.setPrefWidth(500);
		taGistDescription.setPrefHeight(300);
		VBox vbox = new VBox(lblMessage, lblBlank, taGistDescription);
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		Alert      alert   = getAlert(Alert.AlertType.NONE, "", vbox);
		ButtonType proceed = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(proceed, ButtonType.CANCEL);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType>  option      = alert.showAndWait();
		Map<Response, String> responseMap = new HashMap<>();
		if (option.isPresent() && option.get().equals(ButtonType.CANCEL)) {
			return "";
		}
		return taGistDescription.getText();
	}

	public static String showFileRenameAlert(String currentFilename) {
		String message    = "Renaming file " + currentFilename;
		Label  lblMessage = new Label(message);
		lblMessage.setWrapText(true);
		lblMessage.setPrefWidth(300);
		lblMessage.setPrefHeight(60);
		Label     lblBlank       = new Label(" ");
		Label     lblNewFilename = new Label("New Filename:");
		TextField tfFilename     = newTextField(currentFilename, "");
		tfFilename.setPrefWidth(165);
		tfFilename.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty()) tfFilename.setText(currentFilename);
		});
		HBox hbox = new HBox(lblNewFilename, tfFilename);
		hbox.setSpacing(10);
		hbox.setPadding(new Insets(10, 10, 10, 10));
		VBox vbox = new VBox(lblMessage, lblBlank, hbox);
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		Alert      alert   = getAlert(Alert.AlertType.NONE, "", vbox);
		ButtonType proceed = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(proceed, ButtonType.CANCEL);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType> option = alert.showAndWait();
		if (option.isPresent() && option.get().equals(ButtonType.CANCEL)) {
			return "";
		}
		return tfFilename.getText();
	}

	public static String[] newGistAlert() {
		Response response   = Response.CANCELED;
		Alert    alert      = new Alert(Alert.AlertType.NONE);
		Label    lblMessage = new Label("To create a new Gist, Provide the following information, then click Create Gist");
		lblMessage.setWrapText(true);
		Label lblGistDescription = new Label("Gist Description");
		Label lblGistName        = new Label("Gist Name:");
		Label lblFilename        = new Label(" Filename:");
		lblGistName.setMinWidth(75);
		lblFilename.setMinWidth(75);
		lblGistName.setMinHeight(21);
		lblFilename.setMinHeight(21);
		lblGistName.setAlignment(Pos.BOTTOM_RIGHT);
		lblFilename.setAlignment(Pos.BOTTOM_RIGHT);
		CheckBox cbPublic  = new CheckBox("Public");
		CheckBox cbPrivate = new CheckBox("Private");
		cbPublic.selectedProperty().addListener((observable, oldValue, newValue) -> {if (newValue) cbPrivate.setSelected(false);});
		cbPrivate.selectedProperty().addListener((observable, oldValue, newValue) -> {if (newValue) cbPublic.setSelected(false);});
		cbPrivate.setSelected(true);
		HBox stateBox = new HBox(cbPrivate, cbPublic);
		stateBox.setSpacing(20);
		TextField tfFilename = newTextField("File.java", "");
		tfFilename.setPromptText("New Filename");
		tfFilename.textProperty().addListener((observable, oldValue, newValue) -> {if (newValue.isEmpty()) tfFilename.setText("File.java");});
		tfFilename.setMinWidth(200);
		TextField tfGistName = newTextField("New Gist", "");
		tfGistName.setPromptText("Gist Name");
		tfGistName.textProperty().addListener((observable, oldValue, newValue) -> {if (newValue.isEmpty()) tfGistName.setText("New Gist");});
		tfGistName.setMinWidth(200);
		TextArea taGistDescription = new TextArea("Gist Description");
		taGistDescription.textProperty().addListener((observable, oldValue, newValue) -> {if (newValue.isEmpty()) taGistDescription.setText("Gist Description");});
		taGistDescription.setPrefWidth(400);
		taGistDescription.setPrefHeight(400);
		HBox filenameBox = new HBox(lblFilename, tfFilename);
		HBox gistNameBox = new HBox(lblGistName, tfGistName);
		VBox content     = new VBox(lblMessage, stateBox, gistNameBox, filenameBox, lblGistDescription, taGistDescription);
		filenameBox.setSpacing(8);
		gistNameBox.setSpacing(8);
		content.setSpacing(15);
		alert.getDialogPane().setContent(content);
		tfFilename.requestFocus();
		tfFilename.selectAll();
		ButtonType createGist = new ButtonType("Create Gist", ButtonBar.ButtonData.YES);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(createGist, ButtonType.CANCEL);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType> option  = alert.showAndWait();
		String[]             choices = null;
		if (option.isPresent()) {
			if (option.get().equals(createGist)) {
				choices = new String[]{
						(cbPublic.isSelected() ? "Public" : "Private"),
						tfGistName.getText(),
						tfFilename.getText(),
						taGistDescription.getText()
				};
			}
		}
		return choices;
	}

	public static Map<Response, String> showNewFileAlert(String gistName) {
		Response response   = Response.CANCELED;
		Label    lblMessage = new Label("Creating a new file in Gist named:\n\n" + gistName + "\n\nPlease enter a name for this new file then click on Create File");
		lblMessage.setWrapText(true);
		Label     lblFilename = new Label("Filename:");
		TextField tfFilename  = newTextField("", "New Filename");
		HBox      filenameBox = new HBox(lblFilename, tfFilename);
		filenameBox.setSpacing(8);
		VBox content = new VBox(lblMessage, filenameBox);
		content.setSpacing(8);
		Alert alert = getAlert(Alert.AlertType.NONE, "", content);
		alert.getDialogPane().setContent(content);
		tfFilename.requestFocus();
		ButtonType createFile = new ButtonType("Create File", ButtonBar.ButtonData.YES);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(createFile, ButtonType.CANCEL);
		alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
		Optional<ButtonType> option = alert.showAndWait();
		if (option.isPresent() && option.get().equals(createFile)) response = Response.PROCEED;
		Map<Response, String> responseMap = new HashMap<>();
		responseMap.put(response, tfFilename.getText());
		return responseMap;
	}

	public static Boolean showConfirmation(String contentText) {
		return showConfirmation("", contentText);
	}

	public static void showExceptionDialog(Exception e) {
		showExceptionDialog(e, "");
	}

	public static void showExceptionDialog(Exception e, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			addDialogIconTo(alert, false);
			alert.setTitle(applicationTitle);
			alert.setHeaderText("Exception Occurred");
			alert.setContentText(message.isEmpty() ? "An unknown error occurred.\n" + e.getMessage() : message);
			alert.getDialogPane().getScene().getStylesheets().add(LiveSettings.theme.getStyleSheet());
			String exceptionText = e.getLocalizedMessage();

			Label label = new Label("The exception message was:");

			TextArea textArea = new TextArea(exceptionText);
			textArea.setEditable(false);
			textArea.setWrapText(true);

			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);

			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(label, 0, 0);
			expContent.add(textArea, 0, 1);
			alert.getDialogPane().setExpandableContent(expContent);
			alert.getDialogPane().setExpanded(true);
			alert.showAndWait();
		});
	}

	private static void addDialogIconTo(Alert alert, boolean addCustomHeaderGraphic) {
		Platform.runLater(() -> {
			if (iconPath != null && !iconPath.isEmpty()) {
				final Image appIcon     = new Image(iconPath);
				Stage       dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
				dialogStage.getIcons().add(appIcon);

				if (addCustomHeaderGraphic) {
					final ImageView headerIcon = new ImageView(iconPath);
					headerIcon.setFitHeight(48); // Set size to JavaFX API recommendation.
					headerIcon.setFitWidth(48);
					alert.getDialogPane().setGraphic(headerIcon);
				}
			}
		});
	}

	public static void setIconPath(String icon) {
		iconPath = icon;
	}

	public static void setApplicationTitle(String appTitle) {applicationTitle = appTitle;}
}