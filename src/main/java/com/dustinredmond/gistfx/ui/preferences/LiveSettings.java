package com.dustinredmond.gistfx.ui.preferences;

import com.dustinredmond.gistfx.ui.enums.Type;

public class LiveSettings {


	private static final Type                        STANDARD           = Type.STANDARD;
	public static        UISettings.DataSource       dataSource;
	public static        UISettings.DataSource       liveLocation;
	public static        Integer                     saveGitHubInterval = 10;
	public static        UISettings.Theme            theme              = UISettings.Theme.DARK;
	public static        boolean                     overrideSource     = true;
	public static        UISettings.ProgressBarColor progressBarColor;
	public static        Type                        progressBarMode;
	private static       UISettings.LoginScreen      loginScreen;

	public static void applyUserPreferences() {
		if (overrideSource) {
			dataSource = UserPreferences.getLoadSource();
		}
		progressBarMode = UserPreferences.getRandomProgressBarChoice();
		if (progressBarMode.equals(STANDARD)) progressBarColor = UserPreferences.getProgressBarColor();
		setLoginScreen(UserPreferences.getLogonScreenChoice());
		theme              = UserPreferences.getTheme();
		liveLocation       = UserPreferences.getLiveLocation();
		saveGitHubInterval = UserPreferences.getLiveInterval();
	}

	public static UISettings.LoginScreen getLoginScreen() {
		return loginScreen;
	}

	public static void setLoginScreen(UISettings.LoginScreen loginScreen) {
		LiveSettings.loginScreen = loginScreen;
	}
}
