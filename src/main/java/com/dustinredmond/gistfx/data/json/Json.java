package com.dustinredmond.gistfx.data.json;

import com.dustinredmond.gistfx.cryptology.Crypto;
import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.ui.alerts.CustomAlert;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Json {

	private static Map<String, String> nameMap = new HashMap<>();


	private static File         jsonFile;
	private static JsonTemplate jsonTemplate = new JsonTemplate();

	private static void loadFile() {
		if (jsonFile != null) {
			if (jsonFile.exists()) {
				com.google.gson.Gson gson     = new com.google.gson.Gson();
				String               jsonText = Action.loadTextFile(jsonFile);
				if (jsonText.length() > 5) {
					Json.jsonTemplate = gson.fromJson(jsonText, JsonTemplate.class);
					nameMap           = Json.jsonTemplate.getNameMap();
				}
			}
		}
	}

	private static void save() {
		jsonTemplate.setNameMap(nameMap);
		if (jsonFile != null) {
			com.google.gson.Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileWriter writer = new FileWriter(jsonFile.getAbsolutePath())) {
				gson.toJson(Json.jsonTemplate, writer);
			}
			catch (IOException e) {
				CustomAlert.showExceptionDialog(e, "There was a problem saving the Json data file. Check the Access Control List for folder:\n\n" + jsonFile.getAbsolutePath() + "\n\nand make sure the settings are permissive for creating files.");
				e.printStackTrace();
				System.exit(210);
			}
		}
	}

	public static void loadJsonIntoDatabase() {
		if (jsonFile != null) {
			if (jsonFile.exists()) {
				loadFile();
			}
		}
		if (nameMap.size() > 0) {
			for (String gistId : nameMap.keySet()) {
				String eName = nameMap.get(gistId);
				String name  = Crypto.jsonDecrypt(eName);
				Action.addToSQLNameMap(gistId, name);
			}
		}
	}

	public static void getDataFromDatabase() {
		nameMap = Action.getGistNameMap();
		Map<String, String> newMap = new HashMap<>();
		for (String gistId : nameMap.keySet()) {
			String name  = nameMap.get(gistId);
			String eName = Crypto.jsonEncrypt(name);
			newMap.put(gistId, eName);
		}
		nameMap = new HashMap<>(newMap);
		save();
	}

	public static void setName(String gistId, String gistName) {
		String eGistName = Crypto.jsonEncrypt(gistName);
		if (nameMap.containsKey(gistId)) {
			String eName = nameMap.get(gistId);
			if (eName.equals(eGistName)) return;
			nameMap.replace(gistId, eGistName);
		}
		else {
			nameMap.put(gistId, eGistName);
		}
		save();
	}

	public static void removeName(String gistId) {
		nameMap.remove(gistId);
		save();
	}

	public static void reset() {
		if (jsonFile != null) {
			if (jsonFile.exists()) {
				try {
					FileUtils.forceDelete(jsonFile);
					System.out.println("Json file removed.");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void initPath(String path) {
		File jsonPath = new File(path, "Json");
		jsonFile = new File(jsonPath, "NameMap.json");
		try {
			if (!jsonPath.exists()) FileUtils.createParentDirectories(jsonFile);
			loadFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
