package com.dustinredmond.gistfx.ui.preferences;

import com.dustinredmond.gistfx.ui.enums.Type;

import java.util.prefs.Preferences;


public class UserPreferences {


	private static final Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);

	public static String getHashedToken() {
		return prefs.get(SETTING.TOKEN_HASH.Name(), "");
	}

	//GETTERS

	public static void setHashedToken(String tokenHash) {
		clearTokenHash();
		prefs.put(SETTING.TOKEN_HASH.Name(), tokenHash);
	}

	public static String getHashedPassword() {
		return prefs.get(SETTING.PASSWORD_HASH.Name(), "");
	}

	public static UISettings.DataSource getLiveLocation() {
		String option = prefs.get(SETTING.LIVE_DATA.Name(), UISettings.DataSource.LOCAL.Name());
		return UISettings.DataSource.get(option);
	}

	public static void setLiveLocation(UISettings.DataSource option) {
		clearLiveLocation();
		prefs.put(SETTING.LIVE_DATA.Name(), option.Name());
	}

	public static Integer getLiveInterval() {
		String option = prefs.get(SETTING.LIVE_INTERVAL.Name(), "10");
		return Integer.valueOf(option);
	}

	public static void setLiveInterval(Integer seconds) {
		clearLiveInterval();
		prefs.put(SETTING.LIVE_INTERVAL.Name(), String.valueOf(seconds));
	}

	public static UISettings.DataSource getLoadSource() {
		String option = prefs.get(SETTING.LOAD_SOURCE.Name(), UISettings.DataSource.UNKNOWN.Name());
		return UISettings.DataSource.get(option);
	}

	public static void setLoadSource(UISettings.DataSource option) {
		clearLoadSource();
		prefs.put(SETTING.LOAD_SOURCE.Name(), option.Name());
	}

	public static UISettings.ProgressBarColor getProgressBarColor() {
		String option = prefs.get(SETTING.PROGRESS_BAR_COLOR.Name(), UISettings.ProgressBarColor.GREEN.Name());
		return UISettings.ProgressBarColor.get(option);
	}

	public static void setProgressBarColor(UISettings.ProgressBarColor color) {
		clearProgressBarColor();
		prefs.put(SETTING.PROGRESS_BAR_COLOR.Name(), color.Name());
	}

	public static UISettings.LoginScreen getLogonScreenChoice() {
		String option = prefs.get(SETTING.LOGIN_SCREEN.Name(), UISettings.LoginScreen.GRAPHIC.Name());
		return UISettings.LoginScreen.get(option);
	}

	public static Type getRandomProgressBarChoice() {
		String option = prefs.get(SETTING.RANDOM_COLOR_PROGRESS_BARS.Name(), UISettings.ProgressBarRandom.RANDOM.Name());
		return UISettings.ProgressBarRandom.get(option);
	}

	//SETTERS - Always remove before setting

	public static UISettings.Theme getTheme() {
		String option = prefs.get(SETTING.THEME.Name(), UISettings.Theme.LIGHT.Name());
		return UISettings.Theme.get(option);
	}

	public static void setTheme(UISettings.Theme theme) {
		clearTheme();
		prefs.put(SETTING.THEME.Name(), theme.Name());
	}

	public static boolean getFirstRun() {
		String setting = prefs.get(SETTING.FIRST_RUN.Name(), "true");
		return setting.equals("true");
	}

	public static void setFirstRun(boolean setting) {
		clearFirstRun();
		prefs.put(SETTING.FIRST_RUN.Name(), String.valueOf(setting));
	}

	public static boolean getShowButtonBar() {
		String setting = prefs.get(SETTING.BUTTON_BAR.Name(), "false");
		return setting.equals("true");
	}

	public static void setShowButtonBar(boolean setting) {
		clearButtonBar();
		prefs.put(SETTING.BUTTON_BAR.Name(), String.valueOf(setting));
	}

	public static UISettings.LoginScreen getSecurityOption() {
		String option = prefs.get(SETTING.SECURITY_OPTION.Name(), UISettings.LoginScreen.UNKNOWN.Name());
		return UISettings.LoginScreen.get(option);
	}

	public static void setSecurityOption(UISettings.LoginScreen option) {
		clearSecurityOption();
		prefs.put(SETTING.SECURITY_OPTION.Name(), option.Name());
	}

	public static void setPasswordHash(String passwordHash) {
		clearPasswordHash();
		prefs.put(SETTING.PASSWORD_HASH.Name(), passwordHash);
	}

	public static void setLoginScreenChoice(UISettings.LoginScreen option) {
		clearLogonScreenOption();
		prefs.put(SETTING.LOGIN_SCREEN.Name(), option.Name());
	}

	public static void setUseRandomColorProgressBars(UISettings.ProgressBarRandom choice) {
		clearUseRandomColorProgressBars();
		prefs.put(SETTING.RANDOM_COLOR_PROGRESS_BARS.Name(), choice.Name());
	}

	private static void clearLiveLocation() {prefs.remove(SETTING.LIVE_DATA.Name());}

	//REMOVERS

	private static void clearLiveInterval()               {prefs.remove(SETTING.LIVE_INTERVAL.Name());}

	private static void clearLoadSource()                 {prefs.remove(SETTING.LOAD_SOURCE.Name());}

	private static void clearLogonScreenOption()          {prefs.remove(SETTING.LOGIN_SCREEN.Name());}

	private static void clearUseRandomColorProgressBars() {prefs.remove(SETTING.RANDOM_COLOR_PROGRESS_BARS.Name());}

	private static void clearProgressBarColor()           {prefs.remove(SETTING.PROGRESS_BAR_COLOR.Name());}

	public static void clearPasswordHash()                {prefs.remove(SETTING.PASSWORD_HASH.Name());}

	public static void clearTokenHash()                   {prefs.remove(SETTING.TOKEN_HASH.Name());}

	private static void clearTheme()                      {prefs.remove(SETTING.THEME.Name());}

	private static void clearFirstRun() {
		prefs.remove(SETTING.FIRST_RUN.Name());
	}

	private static void clearButtonBar() {
		prefs.remove(SETTING.BUTTON_BAR.Name());
	}

	private static void clearSecurityOption() {prefs.remove(SETTING.SECURITY_OPTION.Name());}

	public static void resetPreferences() {
		clearLiveLocation();
		clearLiveInterval();
		clearLoadSource();
		clearLogonScreenOption();
		clearUseRandomColorProgressBars();
		clearProgressBarColor();
		clearPasswordHash();
		clearTokenHash();
		clearTheme();
		clearFirstRun();
		clearButtonBar();
		clearSecurityOption();

		setLoginScreenChoice(UISettings.LoginScreen.GRAPHIC);
		setLoadSource(UISettings.DataSource.GITHUB);
		setUseRandomColorProgressBars(UISettings.ProgressBarRandom.RANDOM);
		setTheme(UISettings.Theme.DARK);
		setFirstRun(true);
		setShowButtonBar(true);
	}

	enum SETTING {
		PASSWORD_HASH,
		TOKEN_HASH,
		THEME,
		PROGRESS_BAR_THEME,
		PROGRESS_BAR_COLOR,
		RANDOM_COLOR_PROGRESS_BARS,
		LOGIN_SCREEN,
		LIVE_DATA,
		LIVE_INTERVAL,
		LOAD_SOURCE,
		FIRST_RUN,
		BUTTON_BAR,
		SECURITY_OPTION;

		public String Name(SETTING this) {
			return switch (this) {
				case PASSWORD_HASH -> "GFX_PasswordHash";
				case TOKEN_HASH -> "GFX_TokenHash";
				case THEME -> "GFX_Theme";
				case PROGRESS_BAR_THEME -> "GFX_ProgressBarTheme";
				case PROGRESS_BAR_COLOR -> "GFX_ProgressBarColor";
				case RANDOM_COLOR_PROGRESS_BARS -> "GFX_RandomColorProgressBars";
				case LOGIN_SCREEN -> "GFX_LoginScreen";
				case LIVE_DATA -> "GFX_LiveData";
				case LIVE_INTERVAL -> "GFX_LiveInterval";
				case LOAD_SOURCE -> "GFX_LoadSource";
				case FIRST_RUN -> "GFX_FirstRun";
				case BUTTON_BAR -> "GFX_ShowButtonBar";
				case SECURITY_OPTION -> "GFX_SecurityOption";
			};
		}
	}

}
