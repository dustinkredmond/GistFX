module com.dustinredmond.gistfx {

	requires javafx.controls;
	requires javafx.graphics;
	requires java.prefs;
	requires org.apache.commons.codec;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires github.api;
	requires java.desktop;
	requires okhttp3;
	requires javafx.web;
	requires eu.mihosoft.monacofx;
	requires spring.security.crypto;
	requires java.sql;
	requires com.google.gson;


	exports com.dustinredmond.gistfx to javafx.graphics;
	exports com.dustinredmond.gistfx.javafx.controls;
	exports com.dustinredmond.gistfx.github.gist;
	exports com.dustinredmond.gistfx.data.json;


	opens com.dustinredmond.gistfx.github.gist to javafx.base;
	opens com.dustinredmond.gistfx.data.json to com.google.gson;
	exports com.dustinredmond.gistfx.ui.preferences to javafx.graphics;
	exports com.dustinredmond.gistfx.ui.enums to javafx.graphics;
	exports com.dustinredmond.gistfx.utils to javafx.graphics;
	exports com.dustinredmond.gistfx.data to com.google.gson;
	opens com.dustinredmond.gistfx.data to com.google.gson, javafx.base;
	exports com.dustinredmond.gistfx.ui to javafx.graphics;
}