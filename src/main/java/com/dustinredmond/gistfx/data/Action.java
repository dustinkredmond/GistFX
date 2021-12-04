package com.dustinredmond.gistfx.data;

import com.dustinredmond.gistfx.github.gist.Gist;
import com.dustinredmond.gistfx.github.gist.GistFile;
import com.dustinredmond.gistfx.javafx.controls.CustomProgressBar;
import com.dustinredmond.gistfx.ui.enums.Type;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.ProgressBarColor;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import javafx.beans.property.DoubleProperty;
import org.kohsuke.github.GHGist;

import java.io.File;
import java.util.Map;

import static com.dustinredmond.gistfx.data.GitHub.progress;

public class Action {

	/*
		This class protects the SQLite and GitHub classes to ensure consistency and eliminate redundancy.
	 */

	private static final GitHub GITHUB = new GitHub();
	private static final SQLite SQLITE = new SQLite();

	/**
	 * SQLite ONLY Methods
	 */

	public static void deleteDatabaseFile() {
		SQLITE.deleteDatabaseFile();
	}

	public static void setDatabaseConnection() {
		SQLITE.setConnection();
	}

	public static void addGistToSQL(Gist gist) {SQLITE.addGist(gist);}

	public static boolean delete(GistFile file) {
		if (!GITHUB.delete(file)) return false;
		SQLITE.deleteGistFile(file);
		return true;
	}

	public static void addToSQLNameMap(String gistId, String name) {
		SQLITE.addToNameMap(gistId, name);
	}

	public static int newFile(String gistId, String filename, String content) {
		return SQLITE.newFile(gistId, filename, content);
	}

	public static void setDirtyFile(Integer fileId, boolean dirty) {
		SQLITE.setDirtyFile(fileId, dirty);
	}

	public static Map<String, Gist> getGistMap() {
		return SQLITE.getGistMap();
	}

	public static String getGistName(GHGist ghGist) {
		return SQLITE.getGistName(ghGist);
	}

	public static void cleanDatabase() {
		SQLITE.cleanDatabase();
	}

	public static void setGistName(String gistId, String newName) {
		SQLITE.changeGistName(gistId, newName);
	}

	public static Map<String, String> getGistNameMap() {
		return SQLITE.getGistNameMap();
	}


	/*
	 * GitHub ONLY Methods
	 */

	public static GHGist addGistToGitHub(String description, String filename, String content, boolean isPublic) {
		return GITHUB.newGist(description, filename, content, isPublic);
	}

	public static boolean addFileToGist(String gistId, String filename, String content) {
		return GITHUB.addFileToGist(gistId, filename, content);
	}

	public static Integer getForkCount(String gistId) {
		return GITHUB.getForkCount(gistId);
	}

	public static boolean tokenValid(String token) {
		return GitHub.tokenValid(token);
	}

	public static void loadData() {
		GitHub.loadData();
	}

	public static void refreshAllData() {
		GitHub.refreshAllData();
	}


	/*
	 * GitHub AND SQLite Methods
	 */

	public static boolean delete(Gist gist) {
		if (!GITHUB.delete(gist)) return false;
		SQLITE.deleteGist(gist);
		return true;
	}

	public static boolean save(GistFile file, boolean upload) {
		SQLITE.saveFile(file);
		if (upload) {
			return GITHUB.upload(file);
		}
		return true;
	}

	public static boolean save(Gist gist, boolean upload) {
		SQLITE.updateGistDescription(gist);
		if (upload) {
			return GITHUB.upload(gist);
		}
		return true;
	}

	public static void renameFile(GistFile file) {
		if (!GITHUB.renameFile(file)) return;
		SQLITE.renameFile(file);
	}


	/**
	 * Disk and file methods
	 */

	public static String loadTextFile(File file) {
		return Disk.loadTextFile(file);
	}


	/*
	 * None of the above
	 */

	public static CustomProgressBar getProgressNode(double height, Type type, UISettings.ProgressBarColor color) {
		LiveSettings.progressBarMode = type;
		if (LiveSettings.theme.equals(Theme.DARK)) {
			LiveSettings.progressBarColor = ProgressBarColor.ORANGE;
		}
		if (color == null) {
			return new CustomProgressBar(progress, height);
		}
		return new CustomProgressBar(progress, height, color);
	}

	public static DoubleProperty getProgressBinding() {
		progress.setValue(.1);
		return progress;
	}

}
