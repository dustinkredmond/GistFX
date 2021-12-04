package com.dustinredmond.gistfx.javafx.controls;

import com.dustinredmond.gistfx.ui.enums.Type;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ProgressBar;

import java.util.Random;

public class CustomProgressBar extends ProgressBar {

	public CustomProgressBar() {
		super(0);
	}

	public CustomProgressBar(DoubleProperty bindProperty, double height) {
		super(0);
		progressProperty().bind(bindProperty);
		setPrefHeight(height);
		setup();
	}

	public CustomProgressBar(DoubleProperty bindProperty, double height, UISettings.ProgressBarColor color) {
		super(0);
		progressProperty().bind(bindProperty);
		setPrefHeight(height);
		//The next three lines must not be rearranged in their order or else the progress bar won't come back with the chosen color.
		getStylesheets().add(Theme.PROGRESS_BAR.getStyleSheet());
		setTheme();
		getStyleClass().add(color.getStyleClass());
	}

	private void setup() {
		getStylesheets().add(Theme.PROGRESS_BAR.getStyleSheet());

		setTheme();

		if (LiveSettings.progressBarMode.equals(Type.RANDOM)) {
			addRandomColor();
		}
		else {
			addUserColorChoice();
		}
	}

	private void addRandomColor() {
		getStyleClass().add(randomStyleClass());
	}

	private void addUserColorChoice() {
		getStyleClass().add(LiveSettings.progressBarColor.getStyleClass());
	}

	private void setTheme() {
		if (LiveSettings.theme.equals(Theme.DARK)) {
			getStyleClass().add("dark");
		}
		else {
			getStyleClass().add("light");
		}
	}

	private String randomStyleClass() {
		String[] colors = new String[]{
				UISettings.ProgressBarColor.RED.getStyleClass(),
				UISettings.ProgressBarColor.ORANGE.getStyleClass(),
				UISettings.ProgressBarColor.YELLOW.getStyleClass(),
				UISettings.ProgressBarColor.GREEN.getStyleClass(),
				UISettings.ProgressBarColor.BLUE.getStyleClass(),
				UISettings.ProgressBarColor.CYAN.getStyleClass(),
				UISettings.ProgressBarColor.HOTPINK.getStyleClass(),
				UISettings.ProgressBarColor.OCEAN.getStyleClass()
		};

		Random random = new Random();
		return colors[random.nextInt(0, 7)];
	}
}
