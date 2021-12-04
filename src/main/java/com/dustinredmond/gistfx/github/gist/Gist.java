package com.dustinredmond.gistfx.github.gist;

import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.utils.Modify;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;


public class Gist {

	private final StringProperty name        = new SimpleStringProperty();
	private final StringProperty description = new SimpleStringProperty();
	private final String         gistURL;
	private final String         gistId;
	private       boolean        isPublic;
	private       List<GistFile> fileList    = new ArrayList<>();

	public Gist(String gistId, String name, String description, boolean isPublic, String gistURL) {
		this.gistId = gistId;
		this.name.setValue(name);
		this.description.setValue(description);
		this.isPublic = isPublic;
		this.gistURL  = gistURL;
	}

	/*
		Public setters
	 */
	public void addFiles(List<GistFile> fileList) {
		this.fileList = fileList;
	}

	public void addFile(GistFile file) {
		fileList.add(file);
	}

	/*
		Public getters
	 */
	public String getGistId() {return gistId;}

	public String getDescription() {return description.get();}

	public String getURL()         {return gistURL;}

	public String getName()        {return name.getValue();}

	/*
		Changers to GitHub AND SQL
	 */
	public void setName(String newName) {
		name.setValue(newName);
		Action.setGistName(gistId, newName);
	}

	public boolean isPublic() {return isPublic;}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public List<GistFile> getFiles() {return fileList;}

	public void deleteFile(String filename) {
		fileList.removeIf(file -> file.getFilename().equals(filename));
	}

	public boolean setDescription(String description) {
		this.description.setValue(description);
		return Action.save(this, true);
	}

	public int getForkCount() {return Action.getForkCount(gistId);}

	@Override
	public String toString() {return Modify.string().truncate(name.getValue().replaceAll("\\n", " "), 30, true);}
}
