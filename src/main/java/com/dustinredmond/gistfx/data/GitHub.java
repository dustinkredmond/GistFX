package com.dustinredmond.gistfx.data;

import com.dustinredmond.gistfx.github.gist.Gist;
import com.dustinredmond.gistfx.github.gist.GistFile;
import com.dustinredmond.gistfx.github.gist.GistManager;
import com.dustinredmond.gistfx.ui.LoginWindow;
import com.dustinredmond.gistfx.ui.alerts.CustomAlert;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.DataSource;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.HttpClientGitHubConnector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GitHub {

	public static final  DoubleProperty            progress  = new SimpleDoubleProperty(0);
	private static final DataSource                LOCAL     = DataSource.LOCAL;
	private static final DataSource                GITHUB    = DataSource.GITHUB;
	private static       org.kohsuke.github.GitHub gitHub;
	private static       Map<String, GHGist>       ghGistMap = null;

	public static boolean tokenValid(String token) {
		try {
			gitHub = new GitHubBuilder().withOAuthToken(token).withConnector(new HttpClientGitHubConnector()).build();
			return gitHub.isCredentialValid();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void loadData() {
		DataSource dataSource = LiveSettings.dataSource;
		if (dataSource.equals(LOCAL)) {
			new Thread(GitHub::getGHGistMap).start();
			LoginWindow.addInfo("Loading UI");
			GistManager.startFromDatabase();
		}
		if (dataSource.equals(GITHUB)) {
			LoginWindow.addInfo("Downloading Gist Objects");
			getGHGistMap();
			LoginWindow.addInfo("Loading GUI");
			GistManager.startFromGit(ghGistMap, false);
		}
	}

	private static void setProgress(double value) {
		Platform.runLater(() -> progress.setValue(value));
	}

	private static void getGHGistMap() {
		try {
			List<GHGist> list = gitHub.getMyself().listGists().toList();
			double       size = list.size();
			ghGistMap = new HashMap<>();
			for (double x = 0; x < size; x++) {
				String gistId = list.get((int) x).getGistId();
				GHGist ghGist = gitHub.getGist(gistId);
				ghGistMap.put(gistId, ghGist);
				setProgress(x / (size - 2));
			}
			setProgress(0);
		}
		catch (IOException ignored) {
			//    e.printStackTrace();
		}
	}

	public static void refreshAllData() {
		getGHGistMap();
		GistManager.startFromGit(ghGistMap, true);
	}

	public boolean upload(Gist gist) {
		String  gistId      = gist.getGistId();
		String  description = gist.getDescription();
		boolean success     = false;
		try {
			gitHub.getGist(gistId).update().description(description).update();
			success = true;
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return success;
	}

	public boolean upload(GistFile file) {
		boolean success  = false;
		String  gistId   = file.getGistId();
		String  filename = file.getFilename();
		String  content  = file.getContent();
		try {
			gitHub.getGist(gistId).update().updateFile(filename, content).update();
			success = true;
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return success;
	}

	public boolean renameFile(GistFile file) {
		boolean success     = false;
		String  gistId      = file.getGistId();
		String  oldFilename = file.getOldFilename();
		String  newFilename = file.getNewFilename();
		String  content     = file.getContent();
		System.out.println("Old Name: " + oldFilename);
		System.out.println("New Name: " + newFilename);
		try {
			gitHub.getGist(gistId).update().addFile(newFilename, content).update();
			gitHub.getGist(gistId).update().deleteFile(oldFilename).update();
			success = true;
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return success;
	}

	public boolean delete(Gist gist) {
		boolean success = false;
		String  gistId  = gist.getGistId();
		try {
			gitHub.getGist(gistId).delete();
			ghGistMap.remove(gistId);
			success = true;
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return success;
	}

	public boolean delete(GistFile file) {
		boolean success  = false;
		String  gistId   = file.getGistId();
		String  filename = file.getFilename();
		try {
			gitHub.getGist(gistId).update().deleteFile(filename).update();
			System.out.println("gistId: " + gistId);
			System.out.println("Filename: " + filename);
			System.out.println("Github File Deleted");
			success = true;
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return success;
	}

	public GHGist newGist(String description, String filename, String content, boolean isPublic) {
		GHGist ghGist = null;
		try {
			ghGist = gitHub.createGist().public_(isPublic).description(description).file(filename, content).create();
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return ghGist;
	}

	public boolean addFileToGist(String gistId, String filename, String content) {
		boolean success = false;
		try {
			gitHub.getGist(gistId).update().addFile(filename, content).update();
			success = true;
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return success;
	}

	public Integer getForkCount(String gistId) {
		try {
			return gitHub.getGist(gistId).listForks().toList().size();
		}
		catch (IOException e) {
			throwAlert();
			e.printStackTrace();
		}
		return null;
	}

	private void throwAlert() {
		CustomAlert.showWarning("There was a problem accessing GitHub. See help for more information.");
	}

}
