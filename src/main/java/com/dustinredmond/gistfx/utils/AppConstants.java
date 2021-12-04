package com.dustinredmond.gistfx.utils;

import com.dustinredmond.gistfx.Main;

import java.util.Objects;

public class AppConstants {

	public static String htmlFilePath = Objects.requireNonNull(Main.class.getResource("HelpFiles")).toExternalForm().replaceFirst("file:", "");

	public static String getHtmlFilePathWith(String addResource) {
		return htmlFilePath + addResource;
	}

}
