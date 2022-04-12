package com.dustinredmond.gistfx.ui;

import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.github.Languages;
import com.dustinredmond.gistfx.github.gist.Gist;
import com.dustinredmond.gistfx.github.gist.GistFile;
import com.dustinredmond.gistfx.github.gist.GistManager;
import com.dustinredmond.gistfx.github.gist.GistType;
import com.dustinredmond.gistfx.javafx.PaddedGridPane;
import com.dustinredmond.gistfx.ui.alerts.CustomAlert;
import com.dustinredmond.gistfx.ui.alerts.Help;
import com.dustinredmond.gistfx.ui.enums.Response;
import com.dustinredmond.gistfx.ui.enums.Type;
import com.dustinredmond.gistfx.ui.preferences.UISettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import com.dustinredmond.gistfx.ui.preferences.UserPreferences;
import com.dustinredmond.gistfx.utils.Scener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FilenameUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static javafx.scene.layout.AnchorPane.*;

public class GistWindow {

	private static final Response           MISTAKE               = Response.MISTAKE;
	private static final Response           YES                   = Response.YES;
	private static final Response           PROCEED               = Response.PROCEED;
	private final        Label              lblLanguageSetting    = new Label("Recognized Language");
	private final        Label              lblDescription        = new Label();
	private final        CheckBox           publicCB              = new CheckBox("");
	private final        Label              checkBoxLabel         = new Label("Public");
	private final        Label              lblDescriptionLabel   = new Label("Description:");
	private final        Label              lblFileNameLabel      = new Label("Filename:");
	private final        Label              lblGistNameLabel      = new Label("  Gist Name:");
	private final        Label              lblFileName           = new Label();
	private final        Label              lblGistName           = new Label();
	private final        Button             buttonSaveGist        = new Button("Save Gist");
	private final        Button             buttonNewFile         = new Button("New File");
	private final        Button             buttonSaveFile        = new Button("Save File");
	private final        Button             buttonDeleteFile      = new Button("Delete File");
	private final        Button             buttonNewGist         = new Button("New Gist");
	private final        Button             buttonCopyToClipboard = new Button("Copy File To Clipboard");
	private final        Button             buttonDeleteGist      = new Button("Delete Gist");
	private final        ButtonBar          buttonBar             = new ButtonBar();
	private final        MyMenuBar          menuBar               = new MyMenuBar();
	private final        AnchorPane         ap                    = new AnchorPane();
	private final        AnchorPane         apPane                = new AnchorPane();
	private final        ProgressBar        pBar                  = Action.getProgressNode(10, Type.RANDOM, null);
	private              Type               buttonBarType         = Type.GIST;
	private              boolean            showButtonBar         = false;
	private              TreeView<GistType> masterTreeView;
	private              TreeItem<GistType> treeRoot;
	private              GistFile           file;
	private              Gist               gist;
	private              SplitPane          splitPane;
	private              String             gistURL               = "";
	private              boolean            savingData;
	private              Integer            workingSceneId;
	private              Integer            sceneId;
	private              TreeView<GistType> treeView;
	private              TreeItem<GistType> selectedTreeItemForGistName;
	private              TreeItem<GistType> selectedTreeItemForGistFileName;

	private static void setNodePosition(Node node, double left, double right, double top, double bottom) {
		if (top != -1) setTopAnchor(node, top);
		if (bottom != -1) setBottomAnchor(node, bottom);
		if (left != -1) setLeftAnchor(node, left);
		if (right != -1) setRightAnchor(node, right);
	}

	/**
	 * UI Related Methods
	 */

	public void showMainWindow(boolean fromReload) {
		masterTreeView = getTreeView();
		showButtonBar  = UserPreferences.getShowButtonBar();
		placeControlsOnPane();
		setControlVisualProperties();
		setControlActionProperties();
		sceneId = Scener.addScene(ap);
		splitPane.setDividerPosition(0, .2);
		addMenuBarItems();
		Scener.getScene(sceneId).setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.T && e.isMetaDown()) {
				treeView.requestFocus();
			}
			if (e.getCode() == KeyCode.E && e.isControlDown() && e.isAltDown()) {
				CodeEditor.requestFocus();
			}
		});
		Scener.showScene(sceneId);
		Scener.setSceneCloseEvent(sceneId, this::closeWindowEvent);
		if (fromReload) CustomAlert.showInfo("All data re-downloaded successfully.", Scener.getOwner(sceneId));
	}

	private void closeWindowEvent(WindowEvent event) {
		if (GistManager.isDirty()) {
			Response response = CustomAlert.showExitConfirmationResponse("You have unsaved files, how would you like to proceed?\n");
			switch (response) {
				case SAVE -> {
					event.consume();
					saveAllFiles();
					new Thread(() -> {
						while (savingData) sleep(100);
						Scener.exit();
					}).start();
				}
				case CANCELED -> event.consume();
				case EXIT -> System.exit(0);
			}
		}
		else {
			Scener.exit();
		}
	}

	private void closeApp() {
		Window window = Scener.getOwner(sceneId);
		window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	private void placeControlsOnPane() {
		menuBar.addMenuBar();
		addMainNode(menuBar, 0, 0, 0, -1);
		setAnchors(menuBar, 0, 0, 0, -1);
		addMainNode(lblGistNameLabel, 20, -1, 40, -1);
		addMainNode(lblGistName, 105, 150, 40, -1);
		addMainNode(checkBoxLabel, -1, 20, 45, -1);
		addMainNode(lblDescriptionLabel, 20, -1, 80, -1);
		addMainNode(lblDescription, 105, 20, 80, -1);
		addMainNode(pBar, 15, 15, 103, -1);

		addPaneNode(buttonBar, 0, -1, 5, -1);
		addPaneNode(lblFileNameLabel, 0, -1, 35, -1);
		addPaneNode(lblFileName, 85, 20, 35, -1);
		addPaneNode(lblLanguageSetting, 20, -1, 60, -1);
		addPaneNode(CodeEditor.get(), 20, 20, 85, 20);
		setAnchors(CodeEditor.get(), 20, 20, 85, 20);
		splitPane = new SplitPane(masterTreeView, apPane);
		addMainNode(splitPane, -1, -1, -1, -1);
		setAnchors(splitPane, 10, 10, 120, 10);
		buttonBar.setPrefHeight(25);
		buttonBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		lblLanguageSetting.setPrefWidth(155);
		lblFileName.setPrefWidth(200);
		lblDescriptionLabel.setPrefWidth(70);
	}

	private void labelsVisible(boolean visible) {
		lblDescription.setVisible(visible);
		lblDescriptionLabel.setVisible(visible);
		lblFileName.setVisible(visible);
		lblGistName.setVisible(visible);
		lblLanguageSetting.setVisible(visible);
		lblFileNameLabel.setVisible(visible);
		lblGistNameLabel.setVisible(visible);
	}

	private void setControlVisualProperties() {
		lblGistName.setPrefWidth(200);
		lblGistName.setWrapText(false);
		lblGistName.setEllipsisString("...");
		lblGistNameLabel.setAlignment(Pos.CENTER_RIGHT);
		lblGistNameLabel.setPrefWidth(75);
		lblDescriptionLabel.setPrefWidth(75);
		lblDescriptionLabel.setAlignment(Pos.CENTER_RIGHT);
		lblFileNameLabel.setAlignment(Pos.CENTER_LEFT);
		lblFileNameLabel.setPrefWidth(60);
		lblDescription.setWrapText(false);
		lblDescription.setEllipsisString("...");
		buttonSaveGist.setMaxWidth(55);
		buttonNewFile.setMaxWidth(55);
		buttonSaveFile.setMaxWidth(55);
		buttonNewGist.setMaxWidth(55);
		buttonDeleteGist.setMaxWidth(80);
		buttonDeleteFile.setMaxWidth(80);
		buttonSaveGist.setPrefHeight(35);
		lblFileName.setPrefWidth(125);
		lblFileName.setAlignment(Pos.CENTER_LEFT);
		lblFileNameLabel.setPrefWidth(75);
		lblFileNameLabel.setAlignment(Pos.CENTER_RIGHT);
		lblLanguageSetting.setVisible(false);
		lblFileNameLabel.setVisible(false);
		lblFileName.setVisible(false);
		publicCB.setDisable(true);
		checkBoxLabel.setGraphic(publicCB);
		checkBoxLabel.setContentDisplay(ContentDisplay.RIGHT);
		pBar.setPrefHeight(10);
		pBar.setVisible(false);
		labelsVisible(false);
		CodeEditor.get().setVisible(false);
	}

	private void setControlActionProperties() {
		buttonNewGist.setOnAction(e -> NewGist());
		buttonCopyToClipboard.setOnAction(e -> copyFileToClipboard());
		buttonDeleteGist.setOnAction(e -> deleteGist());
		buttonNewFile.setOnAction(e -> newFile());
		buttonSaveFile.setOnAction(e -> saveFile());
		buttonDeleteFile.setOnAction(e -> deleteFile());
		lblFileName.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals(oldValue)) {
				if (file != null) {
					if (selectedTreeItemForGistFileName != null) {
						selectedTreeItemForGistFileName.setValue(new GistType(file));
					}
				}
			}
		});
		lblGistName.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals(oldValue)) {
				if (selectedTreeItemForGistName != null) {
					selectedTreeItemForGistName.setValue(new GistType(gist));
				}
			}
		});
		lblFileName.setOnMouseClicked(e -> renameFile());
		lblGistName.setOnMouseClicked(e -> renameGist());
		lblDescription.setOnMouseClicked(e -> changeGistDescription());
		publicCB.setOnAction(e -> changePublicState());
	}

	private void showWorking() {
		Label lblWorking = new Label("Working");
		lblWorking.setMinWidth(100);
		lblWorking.setMinHeight(50);
		lblWorking.setAlignment(Pos.CENTER);
		Platform.runLater(() -> {
			VBox vbox = new VBox(lblWorking);
			workingSceneId = Scener.addScene(vbox);
			Scener.getStage(workingSceneId).initStyle(StageStyle.TRANSPARENT);
			new Thread(() -> {
				String[] labels = new String[]{"Working", "Working.", "Working..", "Working..."};
				sleep(100);
				while (Scener.getStage(workingSceneId).isShowing()) {
					for (int x = 1; x < 4; x++) {
						String text = labels[x];
						Platform.runLater(() -> lblWorking.setText(text));
						sleep(800);
					}
					for (int x = 2; x >= 0; x--) {
						String text = labels[x];
						Platform.runLater(() -> lblWorking.setText(text));
						sleep(800);
					}
				}
			}).start();
			Scener.showScene(workingSceneId);
		});
	}

	private void handleButtonBar() {
		buttonBar.getButtons().clear();
		if (buttonBarType.equals(Type.FILE)) {
			buttonBar.getButtons().addAll(buttonNewFile, buttonSaveFile, buttonCopyToClipboard, buttonDeleteFile);
		}
		else {
			buttonBar.getButtons().addAll(buttonNewGist, buttonSaveGist, buttonNewFile, buttonDeleteGist);
		}
		if (gist != null || file != null) {buttonBar.setVisible(showButtonBar);}
		double top = buttonBar.isVisible() ? 35 : 5;
		setAnchors(lblFileNameLabel, 0, -1, top, -1);
		setAnchors(lblFileName, 85, 20, top, -1);
		setAnchors(lblLanguageSetting, 20, -1, top + 25, -1);
		setAnchors(CodeEditor.get(), 20, 20, top + 50, 20);
	}

	private void addMainNode(Node node, double left, double right, double top, double bottom) {
		ap.getChildren().add(node);
		setNodePosition(node, left, right, top, bottom);
	}

	private void addPaneNode(Node node, double left, double right, double top, double bottom) {
		apPane.getChildren().add(node);
		setNodePosition(node, left, right, top, bottom);
	}

	private void setAnchors(Node node, double left, double right, double top, double bottom) {
		if (top != -1) AnchorPane.setTopAnchor(node, top);
		if (bottom != -1) AnchorPane.setBottomAnchor(node, bottom);
		if (left != -1) AnchorPane.setLeftAnchor(node, left);
		if (right != -1) AnchorPane.setRightAnchor(node, right);
	}

	/**
	 * Action Methods
	 */

	private void changePublicState() {
		boolean lastState = !publicCB.isSelected();
		if (checkSelectedGist("Change Public State")) {
			if (confirmChangePublicState(gist.getGistId())) {
				new Thread(() -> {
					showWorking();
					TreeItem<GistType> oldBranch = getBranch(gist.getGistId());
					Gist               newGist   = GistManager.setPublicState(gist, publicCB.isSelected());
					if (newGist == null) {
						publicCB.setSelected(lastState);
					}
					else {
						TreeItem<GistType> newBranch = new TreeItem<>(new GistType(newGist));
						for (GistFile file : newGist.getFiles()) {
							TreeItem<GistType> leaf = new TreeItem<>(new GistType(file));
							newBranch.getChildren().add(leaf);
						}
						int index = treeRoot.getChildren().indexOf(oldBranch);
						treeRoot.getChildren().remove(oldBranch);
						treeRoot.getChildren().add(index, newBranch);
						treeView.getSelectionModel().select(newBranch);
						String newState = publicCB.isSelected() ? "Public" : "Secret";
						Platform.runLater(() -> Scener.close(workingSceneId));
						CustomAlert.showInfo("This Gist has been converted to a " + newState + " Gist", Scener.getOwner(sceneId));
					}
				}).start();
			}
			else {
				publicCB.setSelected(lastState);
			}
		}
	}

	private boolean confirmChangePublicState(String gistId) {
		int forkCount = Action.getForkCount(gistId);
		String style = """
				<style>
				.myDiv {
				  background-color: transparent;   \s
				}
				</style>""";
		String message = style + "<div class=\"myDiv\">" +
						 "<h2 style=\"text-align:centerOnScreen\">Change Gist State</h2>\n" +
						 "<body style=\"~background~;~color~\">" +
						 "\n" +
						 "<p>Changing the state of a Gist is a somewhat complex process...</p>\n" +
						 "\n" +
						 "<p>The process that GistFX takes when changing the public state is as follows:</p>\n" +
						 "\n" +
						 "<ol>\n" +
						 "\t<li>A new Gist is created in GitHub.\n" +
						 "\t<ul>\n" +
						 "\t\t<li>The description is set.</li>\n" +
						 "\t\t<li>The new public state is set.</li>\n" +
						 "\t</ul>\n" +
						 "\t</li>\n" +
						 "\t<li>Your Gist files are added to the new Gist.</li>\n" +
						 "\t<li>This GitHub Gist is deleted.</li>\n" +
						 "</ol>\n" +
						 "\n" +
						 "<p>This process might appear to be excessive, but it is necessary because a Gist might have forks, which would create a dichotomous situation if the public state could simply be toggled from public to private. Therefore, deleting the Gist is necessary, which will convert any forks into local copies for those users who have a fork.</p>\n" +
						 "\n" +
						 "<p><strong><span style=\"font-size:18px\">This gist currently has <span style=\"color:#e74c3c\">" + forkCount + "</span> fork(s).</span></strong></p>\n" +
						 "\n" +
						 "<p>Do you wish to proceed?</p>" +
						 "</div>";
		if (UserPreferences.getTheme().equals(Theme.DARK)) {
			message = message
					.replaceAll("~background~", "background-color:#373e43")
					.replaceAll("~color~", "color:lightgrey");
		}
		else {
			message = message
					.replaceAll("~background~", "background-color:#e6e6e6")
					.replaceAll("~color~", "color:black");
		}

		WebView webView = new WebView();
		webView.getEngine().loadContent(message);
		webView.setPrefSize(500, 450);
		return CustomAlert.showConfirmationResponse(webView) == PROCEED;
	}

	private void saveFile() {
		if (checkSelectedGistFile("Save File")) {
			new Thread(() -> {
				sleep(500);
				file.flushDirtyData();
			}).start();
		}
	}

	private void saveAllFiles() {
		if (GistManager.isDirty()) {
			savingData = true;
			new Thread(() -> {
				Platform.runLater(() -> {
					CodeEditor.get().setVisible(false);
					pBar.progressProperty().bind(Action.getProgressBinding());
					Action.getProgressBinding().setValue(0);
					pBar.setVisible(true);
					for (TreeItem<GistType> branch : treeRoot.getChildren()) {
						branch.setExpanded(false);
					}
				});
				List<GistFile> unsavedFileList = GistManager.getUnsavedFiles();
				double         total           = unsavedFileList.size();
				double         count           = 1;
				for (GistFile file : unsavedFileList) {
					double newCount = count;
					Platform.runLater(() -> Action.getProgressBinding().setValue(newCount / total));
					if (!file.flushDirtyData()) return;
					count++;
				}
				Platform.runLater(() -> {
					pBar.setVisible(false);
					Action.getProgressBinding().setValue(0);
					if (CustomAlert.showInfoResponse("All unsaved files were uploaded to GitHub successfully.", Scener.getOwner(sceneId))) {savingData = false;}
				});
			}).start();
		}
		else {CustomAlert.showInfo("No files need to be uploaded to GitHub.", Scener.getOwner(sceneId));}
	}

	private void NewGist() {
		Platform.runLater(() -> {
			String[] choices = CustomAlert.newGistAlert();
			if (choices != null) {
				boolean isPublic    = choices[0].equals("Public");
				String  gistName    = choices[1];
				String  filename    = choices[2];
				String  description = choices[3];
				String  newGistID   = GistManager.addNewGist(gistName, description, filename, getDefaultJavaText(FilenameUtils.getBaseName(filename)), isPublic);
				if (!newGistID.isEmpty()) {addBranchToTree(newGistID);}
			}
		});
	}

	private void newFile() {
		if (checkSelectedGist("New File")) {
			String                gistId      = gist.getGistId();
			Map<Response, String> responseMap = CustomAlert.showNewFileAlert(gist.getName());
			new Thread(() -> {
				for (Response response : responseMap.keySet()) {
					if (response == PROCEED) {
						String   filename = responseMap.get(response);
						String   contents = getDefaultJavaText(FilenameUtils.getBaseName(filename));
						GistFile file     = GistManager.addNewFile(gistId, filename, contents);
						if (file != null) {
							TreeItem<GistType> leaf = new TreeItem<>(new GistType(file));
							Objects.requireNonNull(getBranch(gistId)).getChildren().add(leaf);
							Objects.requireNonNull(getBranch(gistId)).setExpanded(true);
						}
					}
				}
			}).start();
		}
	}

	private void deleteFile() {
		if (checkSelectedGistFile("Delete File")) {
			String gistId = file.getGistId();
			if (CustomAlert.showConfirmation("Are you sure you want to delete the file\n\n" + file.getFilename() + "\n\nFrom Gist: " + lblGistName.getText() + "?")) {
				if (GistManager.deleteFile(file)) {
					removeLeaf(file);
					setSelectedBranchOrLeaf(Objects.requireNonNull(getBranch(gistId)).getValue());
				}
			}
		}
	}

	private void deleteGist() {
		if (checkSelectedGist("Delete Gist")) {
			String   gistId   = gist.getGistId();
			Response response = deleteGistResponse(gistId);
			if (response == YES) {
				if (GistManager.deleteGist(gist)) {
					removeBranch(gist);
					CustomAlert.showInfo("Gist deleted successfully.", Scener.getOwner(sceneId));
				}
			}
			if (response == MISTAKE) deleteGist();
		}
	}

	private Response deleteGistResponse(String gistId) {
		int    forkCount = gist.getForkCount();
		String forkText  = "";
		if (forkCount > 0) {
			forkText = "This Gist currently has " + forkCount + " fork(s). When you delete this Gist, each fork will be converted into a local Gist for those users who have a fork.\n\n";
		}
		StringBuilder sb        = new StringBuilder(forkText);
		List<String>  fileNames = GistManager.getFilenamesFor(gistId);
		sb.append("Are you sure you wish to delete this gist and these files?\n");
		if (fileNames.size() > 10) {
			sb.append(" (partial list)");
		}
		sb.append("\n");
		int max = Math.min(fileNames.size(), 10);
		for (int x = 0; x < max; x++) {
			sb.append("\t").append(fileNames.get(x)).append("\n");
		}
		if (fileNames.size() > 10) sb.append("...");
		sb.append("\n");
		return CustomAlert.showHardConfirmation("Delete Gist", sb.toString());
	}

	private void renameGist() {
		if (checkSelectedGist("Rename Gist")) {
			String gistId  = gist.getGistId();
			String newName = CustomAlert.showChangeGistNameAlert(gist.getName());
			if (!newName.isEmpty()) {
				gist.setName(newName);
				Objects.requireNonNull(getBranch(gistId)).setValue(new GistType(gist));
				lblGistName.setText(newName.replaceAll("\\n", " "));
			}
		}
	}

	private void renameFile() {
		if (checkSelectedGistFile("Rename File")) {
			String newFileName = CustomAlert.showFileRenameAlert(file.getFilename());
			if (!newFileName.isEmpty()) {
				file.renameFile(newFileName);
			}
		}
	}

	private void changeGistDescription() {
		if (checkSelectedGist("Change Description")) {
			String newDescription = CustomAlert.showChangeGistDescriptionAlert(gist.getDescription());
			if (!newDescription.isEmpty()) {
				if (gist.setDescription(newDescription)) {
					lblDescription.setText(newDescription.replaceAll("\\n", " "));
				}
			}
		}
	}

	private void reDownloadAllGists() {
		file = null;
		gist = null;
		pBar.setVisible(true);
		GistManager.unBindFileObjects();
		new Thread(() -> {
			Action.refreshAllData();
			Platform.runLater(() -> Scener.close(sceneId));
			pBar.setVisible(false);
		}).start();
	}

	private boolean checkSelectedGist(String action) {
		if (gist == null) {
			CustomAlert.showWarning(action, "No Gist is currently selected.");
			return false;
		}
		return true;
	}

	private boolean checkSelectedGistFile(String action) {
		if (file == null) {
			CustomAlert.showWarning(action, "No file is currently selected.");
			return false;
		}
		return true;
	}

	private void copyFileToClipboard() {
		if (checkSelectedGistFile("Copy To Clipboard")) {
			Clipboard        clipboard     = Clipboard.getSystemClipboard();
			ClipboardContent content       = new ClipboardContent();
			String           contentString = file.getContent();
			if (contentString.length() > 0) {
				content.putString(contentString);
				clipboard.setContent(content);
				CustomAlert.showInfo("Clipboard", file.getFilename() + " copied to clipboard", Scener.getOwner(sceneId));
			}
		}
	}

	/**
	 * Tree Methods
	 */

	public void setSelectedBranchOrLeaf(GistType treeSelection) {
		GistManager.unBindFileObjects();
		lblFileName.textProperty().unbind();
		lblGistName.setText("");
		lblFileName.setText("");
		publicCB.setDisable(false);
		buttonBarType = treeSelection.getType();
		if (UserPreferences.getTheme().equals(Theme.DARK)) {
			CodeEditor.get().getEditor().setCurrentTheme("vs-dark");
			lblLanguageSetting.setStyle("-fx-text-fill: rgba(155,200,155,1)");
		}
		else {
			CodeEditor.get().getEditor().setCurrentTheme("vs-light");
			lblLanguageSetting.setStyle("-fx-text-fill: rgba(155,0,0,.5)");
		}
		labelsVisible(false);
		switch (treeSelection.getType()) {
			case FILE -> {
				file = treeSelection.getFile();
				gist = treeSelection.getGist();
				lblLanguageSetting.setVisible(true);
				lblFileName.textProperty().bind(file.getNameProperty());
				file.bindLanguageInfoProperty(lblLanguageSetting.textProperty());
				CodeEditor.show();
				CodeEditor.bindDocumentTo(file);
				labelsVisible(true);
				setAnchors(CodeEditor.get(), 20, 20, 120, 20);
			}
			case GIST -> {
				gist = treeSelection.getGist();
				file = null;
				CodeEditor.hide();
				lblGistName.setVisible(true);
				lblGistNameLabel.setVisible(true);
				lblDescriptionLabel.setVisible(true);
				lblDescription.setVisible(true);
				setAnchors(CodeEditor.get(), 20, 20, 20, 20);
			}
		}
		lblGistName.setText(gist.getName().replaceAll("\\n", " "));
		lblDescription.setText(gist.getDescription().replaceAll("\\n", " "));
		gistURL = gist.getURL();
		publicCB.setSelected(gist.isPublic());
		handleButtonBar();
	}

	private void removeLeaf(GistFile file) {
		Objects.requireNonNull(getBranch(file.getGistId())).getChildren().removeIf(leaf -> leaf.getValue().getFile().equals(file));
	}

	private void removeBranch(Gist gist) {
		treeRoot.getChildren().removeIf(branch -> branch.getValue().getGist().equals(gist));
	}

	private void addBranchToTree(String gistId) {
		Gist               gist   = GistManager.getGist(gistId);
		TreeItem<GistType> branch = new TreeItem<>(new GistType(gist));
		for (GistFile file : gist.getFiles()) {
			branch.getChildren().add(new TreeItem<>(new GistType(file)));
		}
		treeRoot.getChildren().add(0, branch);
	}

	private TreeItem<GistType> getBranch(String gistId) {
		for (TreeItem<GistType> branch : treeRoot.getChildren()) {
			if (branch.getValue().getType().equals(Type.GIST) && branch.getValue().getGistID().equals(gistId)) {
				return branch;
			}
		}
		return null;
	}

	private TreeView<GistType> getTreeView() {
		treeView = new TreeView<>();
		treeRoot = new TreeItem<>(new GistType());
		treeView.setRoot(treeRoot);
		treeView.setShowRoot(false);
		for (Gist gist : GistManager.getGists()) {
			TreeItem<GistType> branch = new TreeItem<>(new GistType(gist));
			for (GistFile file : gist.getFiles()) {
				TreeItem<GistType> leaf = new TreeItem<>(new GistType(file));
				leaf.graphicProperty().bind(file.getGraphicNode());
				file.addedToTree();
				branch.getChildren().add(leaf);
				if (file.isDirty()) {
					branch.setExpanded(true);
				}
			}
			treeRoot.getChildren().add(branch);
		}
		treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getClickCount() == 1) {
				TreeItem<GistType> selected = treeView.getSelectionModel().getSelectedItem();
				if (selected != null) {
					if (selected.getValue().getType() == Type.GIST) {
						selectedTreeItemForGistName     = selected;
						selectedTreeItemForGistFileName = null;
					}
					else {
						selectedTreeItemForGistName     = null;
						selectedTreeItemForGistFileName = selected;
					}
					setSelectedBranchOrLeaf(selected.getValue());
				}
			}
		});
		treeView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			boolean enter = e.getCode().getName().equals("Enter");
			if (enter) {
				TreeItem<GistType> selected = treeView.getSelectionModel().getSelectedItem();
				if (selected != null) {
					if (selected.getValue().getType() == Type.GIST) {
						selectedTreeItemForGistName     = selected;
						selectedTreeItemForGistFileName = null;
					}
					else {
						selectedTreeItemForGistName     = null;
						selectedTreeItemForGistFileName = selected;
					}
					setSelectedBranchOrLeaf(selected.getValue());
				}
			}
		});
		return treeView;
	}

	/**
	 * Misc. Methods
	 */

	private String getDefaultJavaText(String name) {
		return "public class " + name + " {\n" +
			   "\n" +
			   "\tpublic static void main(String[] args) {\n" +
			   "\t\tSystem.out.println(\"Hello, World!\");\n" +
			   "\t}\n" +
			   "}";
	}

	private void openGistInWebBrowser() {
		if (checkSelectedGist("Open Browser")) {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI(gistURL));
				}
				catch (IOException | URISyntaxException ioException) {ioException.printStackTrace();}
			}
		}
	}

	private void sleep(long milliseconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Menu Bar Methods
	 */

	private void showSettings() {
		UISettings.showWindow(Scener.getScene(sceneId));
	}

	private void addMenuBarItems() {
		menuBar.addToFileMenu("New File", e -> newFile(), false);
		menuBar.addToFileMenu("Save File", e -> saveFile(), true);
		menuBar.addToFileMenu("Save All Files", e -> saveAllFiles(), false);
		menuBar.addToFileMenu("Open In Browser", e -> openGistInWebBrowser(), false);
		menuBar.addToFileMenu("Delete File", e -> deleteFile(), true);
		menuBar.addToFileMenu("Exit GistFX", e -> closeApp(), false);

		menuBar.addToGistMenu("New Gist", e -> NewGist(), false);
		menuBar.addToGistMenu("Delete Gist", e -> deleteGist(), false);
		menuBar.addToGistMenu("Download Gists", e -> reDownloadAllGists(), false);

		menuBar.addToEditMenu("Copy File To Clipboard", e -> copyFileToClipboard(), false);
		menuBar.addToEditMenu("Save Uncommitted Data", e -> saveAllFiles(), true);
		menuBar.addToEditMenu("UserOptions", e -> showSettings(), false);

		MenuItem miToggleBB = new MenuItem(showButtonBar ? "Hide ButtonBar" : "Show ButtonBar");
		miToggleBB.setOnAction(e -> {
			showButtonBar = !showButtonBar;
			handleButtonBar();
			if (showButtonBar) {miToggleBB.setText("Hide ButtonBar");}
			else {miToggleBB.setText("Show ButtonBar");}
		});
		menuBar.addToViewMenu(miToggleBB, false);
		menuBar.addToViewMenu("Toggle Fullscreen", e -> {
			Scener.getStage(sceneId).setFullScreen(!Scener.getStage(sceneId).isFullScreen());
			if (Scener.getStage(sceneId).isFullScreen()) {
				splitPane.setDividerPosition(0, .15);
			}
			else {
				splitPane.setDividerPosition(0, .2);
			}
		}, false);

		menuBar.addToHelpMenu("GistFX Help", e -> Help.mainOverview(), false);
		menuBar.addToHelpMenu("General Help", e -> Help.generalHelp(), false);
		menuBar.addToHelpMenu("If Something Goes Wrong", e -> Help.somethingWrong(), false);
		menuBar.addToHelpMenu("Token Info", e -> Help.showCreateTokenHelp(), true);
		menuBar.addToHelpMenu("Code Languages", e -> Languages.showCodeInformation(), true);
		menuBar.addToHelpMenu("About this program", e -> {
			PaddedGridPane grid    = new PaddedGridPane(5, 30);
			final int      year    = LocalDate.now().getYear();
			final String   version = getClass().getPackage().getImplementationVersion();
			final Label    text    = new Label(Scener.APP_TITLE + "\nVersion: " + version + "\n");
			final TextArea taLicense = new TextArea(
					"Copyright \u00A9 " + year + "\n\tDustin K. Redmond <dustin@dustinredmond.com>\n\tMichael D. Sims <mike@simtechdata.com>\n\n" +
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
					"FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE\n" +
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
			Integer thisSceneId = Scener.addScene(grid);
			Scener.setTitleContext(thisSceneId, Scener.APP_TITLE);
			Scener.getStage(thisSceneId).initModality(Modality.APPLICATION_MODAL);
			Scener.showOnTop(thisSceneId);
		}, false);
	}

	private static class MyMenuBar extends MenuBar {

		private final Menu menuFile;
		private final Menu menuGist;
		private final Menu menuEdit;
		private final Menu menuView;
		private final Menu menuHelp;

		public MyMenuBar() {
			menuFile = new Menu("File");
			menuGist = new Menu("Gist");
			menuEdit = new Menu("Edit");
			menuView = new Menu("View");
			menuHelp = new Menu("Help");
		}

		private void addMenuBar() {
			this.getMenus().add(menuFile);
			this.getMenus().add(menuGist);
			this.getMenus().add(menuEdit);
			this.getMenus().add(menuView);
			this.getMenus().add(menuHelp);
		}

		private void addToFileMenu(String menuName, EventHandler<ActionEvent> event, boolean separator) {
			MenuItem menuItem = new MenuItem(menuName);
			menuItem.setOnAction(event);
			menuFile.getItems().add(menuItem);
			if (separator) {
				menuFile.getItems().add(new SeparatorMenuItem());
			}
		}

		private void addToEditMenu(String menuName, EventHandler<ActionEvent> event, boolean separator) {
			MenuItem menuItem = new MenuItem(menuName);
			menuItem.setOnAction(event);
			menuEdit.getItems().add(menuItem);
			if (separator) {
				menuEdit.getItems().add(new SeparatorMenuItem());
			}
		}

		private void addToGistMenu(String menuName, EventHandler<ActionEvent> event, boolean separator) {
			MenuItem menuItem = new MenuItem(menuName);
			menuItem.setOnAction(event);
			menuGist.getItems().add(menuItem);
			if (separator) {
				menuGist.getItems().add(new SeparatorMenuItem());
			}
		}

		private void addToHelpMenu(String menuName, EventHandler<ActionEvent> event, boolean separator) {
			MenuItem menuItem = new MenuItem(menuName);
			menuItem.setOnAction(event);
			menuHelp.getItems().add(menuItem);
			if (separator) {
				menuHelp.getItems().add(new SeparatorMenuItem());
			}
		}

		private void addToViewMenu(String menuName, EventHandler<ActionEvent> event, boolean separator) {
			MenuItem menuItem = new MenuItem(menuName);
			menuItem.setOnAction(event);
			menuView.getItems().add(menuItem);
			if (separator) {
				menuView.getItems().add(new SeparatorMenuItem());
			}
		}

		private void addToViewMenu(MenuItem menuItem, boolean separator) {
			menuView.getItems().add(menuItem);
			if (separator) {
				menuView.getItems().add(new SeparatorMenuItem());
			}
		}

	}
}
