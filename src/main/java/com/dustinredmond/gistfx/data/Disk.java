package com.dustinredmond.gistfx.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

class Disk {

	private static StringBuilder sb = new StringBuilder();

	public static String loadTextFile(File file) {
		sb = new StringBuilder();
		try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
			stream.forEach(s -> sb.append(s).append("\n"));
		}
		catch (IOException e) {
			sb = null;
			e.printStackTrace();
		}
		if (sb == null) {return "null";}
		else {return sb.toString();}
	}
}