package com.dustinredmond.gistfx.data;

import com.dustinredmond.gistfx.Main;
import com.dustinredmond.gistfx.cryptology.Crypto;
import com.dustinredmond.gistfx.data.json.Json;
import com.dustinredmond.gistfx.github.gist.Gist;
import com.dustinredmond.gistfx.github.gist.GistFile;
import com.dustinredmond.gistfx.ui.alerts.CustomAlert;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.DataSource;
import org.apache.commons.io.FileUtils;
import org.kohsuke.github.GHGist;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;


class SQLite {


	private final DataSource GITHUB  = DataSource.GITHUB;
	private final DataSource LOCAL   = DataSource.LOCAL;
	private final DataSource UNKNOWN = DataSource.UNKNOWN;
	private       File       sqliteFile;
	private       Connection conn;
	private       String     corePath;


	/*
	 *	Local File operations for SQLite
	 */

	public void setConnection() {
		setSQLFile();
		String  sqlScriptPath = Objects.requireNonNull(Main.class.getResource("SQLite/GistFXCreateSchema.sql")).toExternalForm();
		File    sqlScriptFile = new File(sqlScriptPath.replaceFirst("file:", ""));
		boolean createSchema  = !sqliteFile.exists();
		String  connString    = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();

		corePath = sqlScriptFile.getParent();
		corePath = new File(corePath).getParent();
		Json.initPath(corePath);

		try {
			conn = DriverManager.getConnection(connString);
			conn.setSchema("GistFX");
			conn.setAutoCommit(true);
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println("\n\nSQLite.setDatabaseConnection(): " + e.getMessage() + "\n\n");
			String error = "\n\nThe SQLite library failed to create the database file.\n\tMake sure your Access Control Lists are permissive for the folder that GistFX executes from.\n\nRun program from a command prompt to see the full stack trace.\n\nExiting...";
			System.err.println(error);
			CustomAlert.showWarning(error);
			System.exit(101);
		}
		if (createSchema) {
			String schemaStatements = Disk.loadTextFile(sqlScriptFile);
			if (!createSchema(schemaStatements)) {
				deleteFile(sqliteFile);
				String error = "\n\nThe SQLite library failed to create the schema.\n\tMake sure your Access Control Lists are permissive for the folder that GistFX executes from.\n\nRun program from a command prompt to see the full stack trace.\n\nExiting...";
				System.err.println(error);
				CustomAlert.showWarning(error);
				System.exit(101);
			}
			LiveSettings.dataSource     = GITHUB;
			LiveSettings.overrideSource = false;
		}
		else {
			boolean tablesHaveData = gistsHasData() && gistFilesHasData();
			if (!tablesHaveData) {
				LiveSettings.dataSource     = GITHUB;
				LiveSettings.overrideSource = false;
			}
			else {
				if (LiveSettings.dataSource.equals(UNKNOWN)) {
					LiveSettings.dataSource = LOCAL;
				}
			}
		}
	}

	public String getCorePath() {
		return corePath;
	}

	private void setSQLFile() {
		String sqlPath = Objects.requireNonNull(Main.class.getResource("SQLite")).toExternalForm().replaceFirst("file:", "");
		sqliteFile = new File(sqlPath, "Database.sqlite");
	}

	public void deleteDatabaseFile() {
		setSQLFile();
		deleteFile(sqliteFile);
	}

	private void deleteFile(File file) {
		try {
			FileUtils.forceDelete(file);
		}
		catch (IOException e) {
			System.err.println("SQLite.delete(File): " + e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 *	ENCRYPTED database methods
	 */

	private boolean createSchema(String schemaStatements) {
		boolean result = true;
		schemaStatements = schemaStatements.replaceAll("(.)(\\n)", "$1 ");
		schemaStatements = schemaStatements.replaceAll("CREATE TABLE", "CREATE TABLE IF NOT EXISTS");
		schemaStatements = schemaStatements.replaceAll("(\\()(\\s+)", "$1");
		schemaStatements = schemaStatements.replaceAll("\\n\\n", "\n");
		schemaStatements = schemaStatements.replaceAll(" {2,}", " ");
		schemaStatements = schemaStatements.replaceAll(" \\)", ")");
		String[] createStatements = schemaStatements.split("\n");
		try {
			Statement stmt = conn.createStatement();
			for (String SQL : createStatements) {
				stmt.executeUpdate(SQL);
			}
		}
		catch (SQLException sqe) {
			result = false;
			System.err.println("*** SQLite.createSchema ***\n" + sqe.getMessage());
			sqe.printStackTrace();
		}
		return result;
	}

	/*
	 *  Gist only actions
	 */

	public void addGist(Gist gist) {
		String  gistId       = gist.getGistId();
		String  eDescription = Crypto.encryptWithSessionKey(gist.getDescription());
		boolean isPublic     = gist.isPublic();
		String  eUrl         = Crypto.encryptWithSessionKey(gist.getURL());
		try {
			String            SQL = "INSERT INTO Gists (gistId, description, isPublic, url) VALUES (?,?,?,?)";
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, gistId);
			pst.setString(2, eDescription);
			pst.setBoolean(3, isPublic);
			pst.setString(4, eUrl);
			pst.executeUpdate();
			pst.close();
			addToNameMap(gist.getGistId(), gist.getName());
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public void deleteGist(Gist gist) {
		String gistId = gist.getGistId();
		String SQL    = "DELETE FROM Gists WHERE gistId = ?";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, gistId);
			pst.executeUpdate();
			pst.close();
			Json.removeName(gist.getGistId());
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public void updateGistDescription(Gist gist) {
		String gistId       = gist.getGistId();
		String eDescription = Crypto.encryptWithSessionKey(gist.getDescription());
		String SQL          = "UPDATE Gists SET description = ? WHERE gistId = ?;";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, eDescription);
			pst.setString(2, gistId);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public Map<String, Gist> getGistMap() {

		Map<String, Gist> map = new HashMap<>();
		String            SQL = "SELECT A.gistId, B.name, A.description, A.isPublic, A.url FROM Gists A, NameMap B WHERE A.gistId = B.gistId;";
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				String  gistId      = rs.getString(1);
				String  name        = Crypto.decryptWithSessionKey(rs.getString(2));
				String  description = Crypto.decryptWithSessionKey(rs.getString(3));
				boolean isPublic    = rs.getBoolean(4);
				String  url         = Crypto.decryptWithSessionKey(rs.getString(5));
				Gist    gist        = new Gist(gistId, name, description, isPublic, url);
				map.put(gistId, gist);
			}
			rs.close();
			Map<Integer, GistFile> gistFiles = loadFiles();
			for (Gist gist : map.values()) {
				String         gistId   = gist.getGistId();
				List<GistFile> fileList = new ArrayList<>();
				for (GistFile file : gistFiles.values()) {
					if (file.getGistId().equals(gistId)) {
						fileList.add(file);
					}
				}
				gist.addFiles(fileList);
			}
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return map;
	}

	/*
	 *  NameMap Actions
	 */

	public void addToNameMap(String gistId, String name) {
		String eName = Crypto.encryptWithSessionKey(name);
		String SQL   = "INSERT INTO NameMap (gistId, name) SELECT ?, ? WHERE NOT EXISTS(SELECT 1 FROM NameMap WHERE gistID = ?);";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, gistId);
			pst.setString(2, eName);
			pst.setString(3, gistId);
			pst.executeUpdate();
			pst.close();
			Json.setName(gistId, name);
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public void changeGistName(String gistId, String newName) {
		String eName = Crypto.encryptWithSessionKey(newName);
		String SQL   = "UPDATE NameMap SET name = ? WHERE gistId = ?;";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
			pst.setString(1, eName);
			pst.setString(2, gistId);
			pst.executeUpdate();
			pst.close();
			Json.setName(gistId, newName);
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public String getGistName(GHGist ghGist) {
		String name        = "";
		String description = ghGist.getDescription();
		String gistId      = ghGist.getGistId();
		String SQL         = "SELECT name FROM NameMap WHERE gistId = ?";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, gistId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				name = Crypto.decryptWithSessionKey(rs.getString(1));
			}
			rs.close();
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return name.length() > 0 ? name : description;
	}

	public Map<String, String> getGistNameMap() {
		Map<String, String> map = new HashMap<>();
		String              SQL = "SELECT gistId, name from NameMap";
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				String gistId = rs.getString(1);
				String name   = Crypto.decryptWithSessionKey(rs.getString(2));
				map.put(gistId, name);
			}
			rs.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return map;
	}


	/*
	 *  Gist File Actions
	 */

	public void saveFile(GistFile file) {
		Integer fileId   = file.getFileId();
		String  eContent = Crypto.encryptWithSessionKey(file.getContent());
		boolean dirty    = file.isDirty();
		String  SQL      = "UPDATE GistFiles SET content = ?, dirty = ? WHERE fileId = ?;";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, eContent);
			pst.setBoolean(2, dirty);
			pst.setInt(3, fileId);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public void renameFile(GistFile file) {
		Integer fileId    = file.getFileId();
		String  eFilename = Crypto.encryptWithSessionKey(file.getFilename());
		String  SQL       = "UPDATE GistFiles SET fileName = ? WHERE fileId = ?;";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setString(1, eFilename);
			pst.setInt(2, fileId);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public int newFile(String gistId, String filename, String content) {
		String eFilename = Crypto.encryptWithSessionKey(filename);
		String eContent  = Crypto.encryptWithSessionKey(content);
		int    fileId    = 0;
		String SQL       = "INSERT INTO GistFiles (gistId, fileName, content) VALUES (?,?,?)";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
			pst.setString(1, gistId);
			pst.setString(2, eFilename);
			pst.setString(3, eContent);
			pst.executeUpdate();
			ResultSet rs = getLastIDResultSet();
			assert rs != null;
			rs.next();
			fileId = rs.getInt(1);
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return fileId;
	}

	public void deleteGistFile(GistFile file) {
		Integer fileId = file.getFileId();
		String  SQL    = "DELETE FROM GistFiles WHERE fileId = ?";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setInt(1, fileId);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	private Map<Integer, GistFile> loadFiles() {
		Map<Integer, GistFile> map = new HashMap<>();
		String                 SQL = "SELECT fileID, gistId, fileName, content, dirty FROM GistFiles";
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				Integer  fileId   = rs.getInt(1);
				String   gistId   = rs.getString(2);
				String   filename = Crypto.decryptWithSessionKey(rs.getString(3));
				String   content  = Crypto.decryptWithSessionKey(rs.getString(4));
				boolean  dirty    = rs.getBoolean(5);
				GistFile gistFile = new GistFile(fileId, gistId, filename, content, dirty);
				map.put(fileId, gistFile);
			}
			rs.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return map;
	}


	/*
	 *	NON-encrypted database methods
	 */

	private boolean gistsHasData() {
		boolean hasData = false;
		String  SQL     = "SELECT count(*) FROM Gists;";
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			if (rs.next()) {
				hasData = rs.getInt(1) > 0;
			}
			rs.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return hasData;
	}

	private boolean gistFilesHasData() {
		boolean hasData = false;
		String  SQL     = "SELECT count(*) FROM GistFiles;";
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			if (rs.next()) {
				hasData = rs.getInt(1) > 0;
			}
			rs.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
		return hasData;
	}

	private ResultSet getLastIDResultSet() {
		try {
			return conn.prepareStatement("select last_insert_rowid();").executeQuery();
		}
		catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return null;
	}

	public void setDirtyFile(Integer fileId, boolean dirty) {
		String SQL = "UPDATE GistFiles SET dirty = ? WHERE fileId = ?;";
		try {
			PreparedStatement pst = conn.prepareStatement(SQL);
			pst.setBoolean(1, dirty);
			pst.setInt(2, fileId);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException sqe) {sqe.printStackTrace();}
	}

	public void cleanDatabase() {
		String cleanNameMap = "DELETE FROM NameMap";
		String cleanGists   = "DELETE FROM Gists";
		String cleanFiles   = "DELETE FROM GistFiles";
		try {
			conn.createStatement().executeUpdate(cleanNameMap);
			conn.createStatement().executeUpdate(cleanGists);
			conn.createStatement().executeUpdate(cleanFiles);
		}
		catch (SQLException throwables) {throwables.printStackTrace();}
	}

}
