package com.dustinredmond.gistfx.github.gist;

import com.dustinredmond.gistfx.Main;
import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.ui.CodeEditor;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.io.FilenameUtils;

import java.util.Timer;
import java.util.TimerTask;

public class GistFile {

	private final UISettings.DataSource LOCAL            = UISettings.DataSource.LOCAL;
	private final UISettings.DataSource GITHUB           = UISettings.DataSource.GITHUB;
	private final StringProperty        filename         = new SimpleStringProperty();
	private final StringProperty        content          = new SimpleStringProperty();
	private final StringProperty        languageInfo     = new SimpleStringProperty();
	private final BooleanProperty       dirty            = new SimpleBooleanProperty(false);
	private final ObjectProperty<Node>  graphicNode      = new SimpleObjectProperty<>();
	private final Integer               fileId;
	private       ImageView             redLight;
	private       ImageView             greyLight;
	private       String                gistId;
	private       String                lastLocalCommit  = "";
	private       String                lastGitHubCommit = "";
	private       String                newFilename;
	private       String                oldFilename;
	private       boolean               saveToGitHub     = LiveSettings.liveLocation.equals(GITHUB);
	private       Timer                 saveLocallyTimer = new Timer();
	private       Timer                 saveGitHubTimer  = new Timer();

	public GistFile(String gistId, String filename, String content, Integer fileId, boolean isDirty) { //used for new files being added to Gist
		this.gistId = gistId;
		this.filename.setValue(filename);
		this.content.setValue(content);
		this.fileId = fileId;
		this.dirty.setValue(isDirty);
		init();
	}

	public GistFile(Integer fileId, String gistId, String filename, String content, boolean isDirty) { //used for new files being added to Gist
		this.gistId = gistId;
		this.filename.setValue(filename);
		this.content.setValue(content);
		this.fileId = fileId;
		this.dirty.setValue(isDirty);
		init();
	}

	private void init() {
		dirty.addListener((observable, oldValue, newValue) -> {
			if (newValue != null && oldValue != null) {
				if (!oldValue.equals(newValue)) {
					boolean setGrey = LiveSettings.liveLocation.equals(LOCAL);
					if (setGrey) {
						Platform.runLater(() -> graphicNode.set((newValue) ? redLight : null));
					}
					else {
						Platform.runLater(() -> graphicNode.set((newValue) ? greyLight : null));
					}
					Action.setDirtyFile(fileId, newValue);
				}
			}
		});
		lastLocalCommit = content.getValue();
		String redLightPath  = Main.class.getResource("RedLightIconTiny.png").toExternalForm();
		String greyLightPath = Main.class.getResource("GreyLightIconTiny.png").toExternalForm();
		Image  redImage      = new Image(redLightPath);
		Image  greyImage     = new Image(greyLightPath);
		redLight  = new ImageView(redImage);
		greyLight = new ImageView(greyImage);
	}

	/*
		Private getters
	 */


	private String getFileExtension() {
		boolean noDot = !filename.getValue().contains(".");
		if (noDot) {
			return "";
		}
		return FilenameUtils.getExtension(filename.getValue());
	}

	/*
		Property binders and related
	 */

	public void unbind() {
		content.unbind();
		languageInfo.setValue("");
		languageInfo.unbind();
		saveLocallyTimer.cancel();
		saveGitHubTimer.cancel();
	}

	private TimerTask saveContentLocally() {
		return new TimerTask() {
			@Override public void run() {
				if (!content.getValue().equals(lastLocalCommit)) {
					dirty.setValue(true);
					updateDatabase();
					lastLocalCommit = content.getValue();
					if (LiveSettings.liveLocation.equals(GITHUB) && !saveToGitHub) {
						saveToGitHub    = true;
						saveGitHubTimer = new Timer(false);
						saveGitHubTimer.scheduleAtFixedRate(saveContentToGitHub(), 0, LiveSettings.saveGitHubInterval * 1000);
					}
				}
			}
		};
	}

	private TimerTask saveContentToGitHub() {
		return new TimerTask() {
			@Override public void run() {
				if (!content.getValue().equals(lastGitHubCommit)) {
					lastGitHubCommit = content.getValue();
					if (!updateGitHub()) {
						saveToGitHub = false;
						saveGitHubTimer.cancel();
					}
					if (LiveSettings.liveLocation.equals(LOCAL) && saveToGitHub) {
						saveToGitHub = false;
						saveGitHubTimer.cancel();
					}
				}
			}
		};
	}

	public void bindContentTo(StringProperty stringProperty) {
		stringProperty.setValue(content.getValue());
		content.bind(stringProperty);
		saveLocallyTimer = new Timer(false);
		saveLocallyTimer.scheduleAtFixedRate(saveContentLocally(), 0, 1500);
		if (LiveSettings.liveLocation.equals(GITHUB)) {
			saveToGitHub    = true;
			saveGitHubTimer = new Timer(false);
			saveGitHubTimer.scheduleAtFixedRate(saveContentToGitHub(), 0, LiveSettings.saveGitHubInterval * 1000);
		}
	}

	public void addedToTree() {
		Platform.runLater(() -> {
			boolean isDirty  = dirty.getValue();
			boolean setDirty = LiveSettings.liveLocation.equals(LOCAL);
			graphicNode.set((isDirty && setDirty) ? redLight : null);
		});
	}

	public void bindLanguageInfoProperty(StringProperty languageInfoProperty) {
		languageInfoProperty.bind(this.languageInfo);
		CodeEditor.setLanguage(getFileExtension());
		languageInfo.setValue("Detected language: " + CodeEditor.getLanguage());
	}

	/*
		Public setters
	 */

	public String getContent() {
		return content.getValue();
	}

	/*
		Public getters
	 */

	public boolean isDirty() {
		return dirty.getValue();
	}

	public String getFilename() {
		return filename.getValue();
	}

	public String getGistId() {
		return gistId;
	}

	public void setGistId(String newGistId) {
		this.gistId = newGistId;
	}

	public Integer getFileId()     {return fileId;}

	public String getNewFilename() {return newFilename;}

	public String getOldFilename() {return oldFilename;}

	public StringProperty getNameProperty() {
		return filename;
	}

	public ObjectProperty<Node> getGraphicNode() {return graphicNode;}

	/*
		SQL Actions
	 */

	private void updateDatabase() {
		Action.save(this, false);
	}

	private boolean updateGitHub() {
		if (Action.save(this, true)) {
			dirty.setValue(false);
			return true;
		}
		return false;
	}

	public boolean flushDirtyData() {
		if (updateGitHub()) {
			dirty.setValue(false);
			return true;
		}
		return false;
	}

	public void delete() {
		Action.delete(this);
	}

	public void renameFile(String newFilename) {
		this.newFilename = newFilename;
		this.oldFilename = filename.getValue();
		filename.setValue(newFilename);
		CodeEditor.setLanguage(getFileExtension());
		Action.renameFile(this);
	}

	@Override
	public String toString() {
		return filename.getValue();
	}
}

