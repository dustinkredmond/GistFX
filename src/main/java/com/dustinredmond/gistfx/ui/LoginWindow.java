package com.dustinredmond.gistfx.ui;

import com.dustinredmond.gistfx.Main;
import com.dustinredmond.gistfx.cryptology.Crypto;
import com.dustinredmond.gistfx.data.Action;
import com.dustinredmond.gistfx.javafx.PasswordDialog;
import com.dustinredmond.gistfx.javafx.controls.CustomProgressBar;
import com.dustinredmond.gistfx.ui.alerts.CustomAlert;
import com.dustinredmond.gistfx.ui.alerts.Help;
import com.dustinredmond.gistfx.ui.enums.LoginStates;
import com.dustinredmond.gistfx.ui.enums.Response;
import com.dustinredmond.gistfx.ui.enums.Type;
import com.dustinredmond.gistfx.ui.preferences.LiveSettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings;
import com.dustinredmond.gistfx.ui.preferences.UISettings.LoginScreen;
import com.dustinredmond.gistfx.ui.preferences.UISettings.Theme;
import com.dustinredmond.gistfx.ui.preferences.UserPreferences;
import com.dustinredmond.gistfx.utils.Scener;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.util.Objects;

import static com.dustinredmond.gistfx.ui.enums.LoginStates.*;
import static javafx.scene.layout.AnchorPane.*;

/**
 * This class needs a little discussion ... there are two methods which handle the 'busy' work of back end tasks. One is preCheck and the other, postCheck. preCheck
 * gathers the information we need to set up the login screen based on what the user has set up (save token locally or not etc.)  While post check handles all the scenarios that
 * can happen when the user either wants to log in, or create a new password etc. It goes through each possible scenario and returns an appropriate response which we then can act
 * on. For example, if the user has a password hash stored AND an encrypted token, then it returns the fact that both exist. If only one or the other exists, then it returns a
 * message indicating a need for reset.
 * <p>
 * Once the state of the users token and password are known, the appropriate action can be taken such as prompt for a password to log in, or prompt for a token, or inform the user
 * that something was wrong, and they need to reset their password etc. and THAT stuff is done in postCheck ... when the user enters the appropriate authentication (or new password etc.)
 * we call the postCheck method which handles the appropriate action based on what the user is trying to do ... validate their local password, decrypt their token and check that it's
 * valid with GitHub, or go through the process of resetting hte users password etc. etc. And each action has a response that we can act on if necessary.
 * <p>
 * Finally, we have buildScene(), which builds the GUI login window based on the results of the preCheck, and then logIntoGitHub() which finishes everything off when
 * everything checks out and is in order.
 * <p>
 */

public class LoginWindow {

	static final         TextArea          taInfo               = new TextArea();
	private static final Label             lblProgress          = new Label(" ");
	private static final Label             lblLoggedIn          = new Label(" ");
	private static       Integer           sceneId              = 100;
	private final        StringProperty    loginPassword        = new SimpleStringProperty("");
	private final        StringProperty    userToken            = new SimpleStringProperty("");
	private final        BooleanProperty   saved                = new SimpleBooleanProperty();
	private final        LoginStates       precheckState;
	private final        String            questionMarkIconPath = Objects.requireNonNull(Main.class.getResource("HelpFiles/QuestionMarkIcon.png")).toExternalForm();
	private final        TextField         tfToken;
	private final        PasswordField     tfPassword;
	private final        CheckBox          cbSave;
	private final        Button            buttonLogin;
	private final        AnchorPane        ap                   = new AnchorPane();
	private final        LoginScreen       PASSWORD_LOGIN       = LoginScreen.PASSWORD_LOGIN;
	private final        LoginScreen       TOKEN_LOGIN          = LoginScreen.TOKEN_LOGIN;
	private final        LoginScreen       UNKNOWN              = LoginScreen.UNKNOWN;
	private final        LoginScreen       currentSecurityMode  = UserPreferences.getSecurityOption();
	private              int               passwordAttempts     = 0;
	private              boolean           usingLocalCreds;
	private              boolean           skip                 = false;
	private              boolean           tokenChecked         = false;
	private              boolean           tokenValid           = false;
	private              LoginStates       postState;
	private              CustomProgressBar pBar;
	private              HBox              hboxToken;
	private              HBox              hboxPassword;
	private              HBox              hboxTop;
	private              HBox              hboxBottom;
	private              HBox              hboxBlank;
	private              HBox              hboxPBar;
	private              LoginScreen       newSecurityMode      = UNKNOWN;
	private              boolean           passwordChanged      = false;

	/**
	 * For preCheck and password state check
	 */
	private String  hashedPassword    = "";
	private String  hashedAccessToken = "";
	private boolean hasHashedPassword;
	private boolean hasHashedToken;
	private boolean noHashedToken;
	private boolean noHashedPassword;
	private boolean saveToken;
	private boolean illegalPassword;
	private boolean tooManyPasswords;
	private boolean noTypedPassword;
	private boolean hasTypedToken;
	private boolean hasTypedPassword;
	private boolean noTypedToken;


	public LoginWindow() {
		Scener.setTitleContext(sceneId, "Login");
		taInfo.setDisable(true);
		assert LiveSettings.getLoginScreen() != null;
		String styleClass = LiveSettings.getLoginScreen().equals(LoginScreen.STANDARD) ? "standard" : "graphic";
		tfToken    = newTextField("", "GitHub Token", styleClass);
		tfPassword = newPasswordField("GistFX Password", styleClass);
		tfPassword.setText("");
		cbSave          = new CheckBox("Save Access Token");
		buttonLogin     = new Button("Login to GitHub");
		precheckState   = preCheck();
		usingLocalCreds = precheckState.equals(HAS_LOCAL_CREDS);
		setControlProperties();
		assert LiveSettings.getLoginScreen() != null;
		if (UserPreferences.getFirstRun()) {
			Help.showIntro();
		}
		if (LiveSettings.getLoginScreen().equals(LoginScreen.STANDARD)) {
			LiveSettings.theme = UserPreferences.getTheme();
			guiLogin();
		}
		else {
			LiveSettings.theme = Theme.DARK;
			graphicLogin();
		}
	}

	private static TextField newTextField(String text, String prompt, String styleClass) {
		TextField tf = new TextField(text);
		tf.setPromptText(prompt);
		tf.getStylesheets().add(Theme.TEXT_FIELD.getStyleSheet());
		tf.getStyleClass().add(styleClass);
		Tooltip.install(tf, new Tooltip("Enter required information then press Enter"));
		return tf;
	}

	private static PasswordField newPasswordField(String prompt, String styleClass) {
		PasswordField pf = new PasswordField();
		pf.setPromptText(prompt);
		pf.getStylesheets().add(Theme.TEXT_FIELD.getStyleSheet());
		pf.getStyleClass().add(styleClass);
		Tooltip.install(pf, new Tooltip("Enter required information then press Enter"));
		return pf;
	}

	public static void addInfo(String info) {
		if (info.startsWith("clear")) {
			taInfo.clear();
		}
		else {taInfo.appendText(info + "\n");}
		Platform.runLater(() -> {
			if (info.startsWith("clear")) {lblProgress.setText("");}
			else {lblProgress.setText(info);}
		});
	}

	private void setControlProperties() {
		saved.bind(cbSave.selectedProperty());
		cbSave.setSelected(usingLocalCreds);
		loginPassword.bind(tfPassword.textProperty());
		userToken.bindBidirectional(tfToken.textProperty());
		buttonLogin.setOnAction(e -> processCredentials());
		tfToken.setOnAction(e -> {
			tfToken.setDisable(true);
			processCredentials();
		});
		tfPassword.setOnAction(e -> {
			tfPassword.setDisable(true);
			processCredentials();
		});
		tfToken.setMinWidth(200);
		tfPassword.setMinWidth(200);
		tfToken.setPromptText("GitHub Token");
		tfPassword.setPromptText("GistFX Password");
	}

	private HBox newHBox(Node... nodes) {
		HBox hbox = new HBox(nodes);
		hbox.setSpacing(5);
		hbox.setAlignment(Pos.CENTER);
		hbox.setPadding(new Insets(10, 10, 10, 10));
		return hbox;
	}

	private VBox newVBox(Node... nodes) {
		VBox vbox = new VBox(nodes);
		vbox.setSpacing(-8);
		vbox.setPadding(new Insets(0, 0, 0, 0));
		return vbox;
	}

	private boolean confirmRemovePassword() {
		return CustomAlert.showConfirmation("Remove Password", """
				Are you sure you want to remove your GistFX password AND your personal access token?

				If you answer Yes, then you will need to enter your personal access token each time you run GistFX.
				However, you can optionally re-check save mode, then provide valid token and set a new password.

				Clicking No will leave everything as it is now.""");
	}

	private void guiLogin() {
		pBar = Action.getProgressNode(13, Type.STANDARD, UISettings.ProgressBarColor.BLACK);
		boolean darkTheme = LiveSettings.theme.equals(Theme.DARK);
		Label   blank1    = new Label(" ");
		Label   blank2    = new Label(" ");
		blank1.setPrefWidth(145);
		blank2.setPrefWidth(150);
		blank2.setMinHeight(26);
		Image     imageQMark = new Image(questionMarkIconPath);
		ImageView ivQMark    = new ImageView(imageQMark);
		ivQMark.setPreserveRatio(true);
		ivQMark.setFitWidth(40);
		ivQMark.setOnMouseClicked(e -> Help.showCreateTokenHelp());
		Label lblToken    = new Label("GitHub Access Token:");
		Label lblPassword = new Label("GistFX Login Password:");
		lblToken.setMinWidth(135);
		lblPassword.setMinWidth(135);
		lblToken.setAlignment(Pos.CENTER_RIGHT);
		lblPassword.setAlignment(Pos.CENTER_RIGHT);
		lblLoggedIn.setMinWidth(150);
		lblLoggedIn.setAlignment(Pos.CENTER_LEFT);
		lblProgress.setMinWidth(150);
		lblProgress.setAlignment(Pos.CENTER_LEFT);
		if (darkTheme) {lblLoggedIn.setStyle("-fx-text-fill: lightgreen");}
		else {lblLoggedIn.setStyle("-fx-text-fill: green");}
		hboxTop      = newHBox(cbSave, blank1, ivQMark);
		hboxToken    = newHBox(lblToken, tfToken);
		hboxPassword = newHBox(lblPassword, tfPassword);
		hboxBlank    = newHBox(blank2);
		hboxBottom   = newHBox(lblLoggedIn, lblProgress);
		hboxBottom.setAlignment(Pos.CENTER_LEFT);
		pBar.setMinWidth(330);
		hboxPBar = newHBox(pBar);
		hboxPBar.setPadding(new Insets(10, 20, 10, 20));
		sceneId = Scener.addScene(new VBox(), e -> System.exit(242));
		if (usingLocalCreds) {
			buildScene(BUILD_PASSWORD_ONLY);
			tfPassword.requestFocus();
		}
		else {
			buildScene(BUILD_TOKEN_ONLY);
			tfToken.requestFocus();
		}
		saved.addListener((observable, oldValue, newValue) -> {
			if (tooManyPasswords) return;
			if (skip) {
				skip = false;
				return;
			}
			boolean wasChecked = oldValue && !newValue;
			if (wasChecked && usingLocalCreds) {
				if (confirmRemovePassword()) {
					passwordChanged = true;
					UserPreferences.clearPasswordHash();
					UserPreferences.clearTokenHash();
					tfPassword.clear();
					tfToken.clear();
					buildScene(BUILD_TOKEN_ONLY);
					usingLocalCreds = false;
				}
				else {
					skip = true;
					cbSave.setSelected(true);//This will trigger this listener again, but the skip boolean will cause it to dump out.
					tfToken.requestFocus();
				}
			}
			else {
				if (newValue) {buildScene(BUILD_BOTH);}
				else {buildScene(BUILD_TOKEN_ONLY);}
			}
			tfToken.requestFocus();
		});
		Scener.showScene(sceneId);
		cbSave.setSelected(currentSecurityMode == PASSWORD_LOGIN || currentSecurityMode == UNKNOWN);
	}

	private void buildScene(LoginStates option) {
		VBox content = newVBox(hboxTop);
		if (option == BUILD_TOKEN_ONLY || option == BUILD_BOTH) {
			content.getChildren().add(hboxToken);
		}
		if (option == BUILD_PASSWORD_ONLY || option == BUILD_BOTH) {
			content.getChildren().add(hboxPassword);
		}
		if ((option == BUILD_PASSWORD_ONLY || option == BUILD_TOKEN_ONLY) && !usingLocalCreds) content.getChildren().add(hboxBlank);
		lblLoggedIn.setMinHeight(26);
		content.getChildren().addAll(hboxPBar, hboxBottom);
		buttonLogin.setDisable(false);
		Scener.setRoot(sceneId, content);
		Scener.showScene(sceneId);
		Scener.resizeWindow(sceneId);
	}

	private void graphicLogin() {
		pBar = Action.getProgressNode(13, Type.RANDOM, null);
		ap.setStyle("-fx-background-color: black");
		sceneId = Scener.addScene(ap, 700, 323);

		Scener.getStage(sceneId).initStyle(StageStyle.TRANSPARENT);
		Scener.getStage(sceneId).setOnCloseRequest(e -> System.exit(108));

		String    pathBoth       = Objects.requireNonNull(Main.class.getResource("LoginForm/Background/BackBoth.png")).toExternalForm();
		String    pathPassword   = Objects.requireNonNull(Main.class.getResource("LoginForm/Background/BackPassword.png")).toExternalForm();
		String    pathToken      = Objects.requireNonNull(Main.class.getResource("LoginForm/Background/BackToken.png")).toExternalForm();
		Image     imageBoth      = new Image(pathBoth);
		Image     imagePassword  = new Image(pathPassword);
		Image     imageToken     = new Image(pathToken);
		ImageView ivBackBoth     = new ImageView(imageBoth);
		ImageView ivBackPassword = new ImageView(imagePassword);
		ImageView ivBackToken    = new ImageView(imageToken);
		String    chkBoxPath     = Objects.requireNonNull(Main.class.getResource("LoginForm/Checkbox.png")).toExternalForm();
		String    qmPath         = Objects.requireNonNull(Main.class.getResource("LoginForm/QuestionMark.png")).toExternalForm();
		String    chkMarkPath    = Objects.requireNonNull(Main.class.getResource("LoginForm/chkMark.png")).toExternalForm();
		Image     imgCheckBox    = new Image(chkBoxPath);
		Image     imgQMark       = new Image(qmPath);
		Image     imgCheckMark   = new Image(chkMarkPath);
		ImageView ivCheckBox     = new ImageView(imgCheckBox);
		ImageView ivQMark        = new ImageView(imgQMark);
		ImageView ivCheckMark    = new ImageView(imgCheckMark);


		ivCheckBox.setPreserveRatio(true);
		ivQMark.setPreserveRatio(true);
		ivQMark.setFitWidth(75);
		ivQMark.setFitHeight(75);
		ivCheckMark.setFitWidth(45);
		ivCheckMark.setFitHeight(45);
		ivCheckBox.setFitWidth(40);
		ivCheckBox.setFitHeight(40);
		ivQMark.setOnMouseClicked(e -> Help.showCreateTokenHelp());
		ivBackToken.setPreserveRatio(true);
		ivBackPassword.setPreserveRatio(true);
		ivBackBoth.setPreserveRatio(true);
		ivBackToken.setFitWidth(700);
		ivBackPassword.setFitWidth(700);
		ivBackBoth.setFitWidth(700);
		ivBackToken.setFitHeight(323);
		ivBackPassword.setFitHeight(323);
		ivBackBoth.setFitHeight(323);

		addAPNode(ivBackToken, 0, 0, 0, 0);
		addAPNode(ivBackPassword, 0, 0, 0, 0);
		addAPNode(ivBackBoth, 0, 0, 0, 0);
		addAPNode(ivCheckBox, 15, -1, 18, -1);
		addAPNode(ivCheckMark, 15, -1, 15, -1);
		addAPNode(ivQMark, -1, 30, -1, 30);
		addAPNode(tfToken, 184, -1, 216, -1);
		addAPNode(tfPassword, 184, -1, 269, -1);
		addAPNode(taInfo, 20, 250, 90, 135);
		addAPNode(pBar, 20, 20, -1, 118);

		taInfo.getStylesheets().add(Theme.TEXT_AREA.getStyleSheet());
		tfToken.setMinHeight(40);
		tfToken.setMinWidth(400);
		tfPassword.setMinHeight(40);
		tfPassword.setMinWidth(400);
		ivCheckBox.setOnMouseClicked(e -> cbSave.setSelected(true));
		ivCheckMark.setOnMouseClicked(e -> cbSave.setSelected(false));

		saved.addListener((observable, oldValue, newValue) -> {
			LoginStates option;
			if (skip) {
				skip = false;
				return;
			}
			boolean wasChecked = oldValue && !newValue;
			if (wasChecked && precheckState == HAS_LOCAL_CREDS) {
				if (confirmRemovePassword()) {
					passwordChanged = true;
					UserPreferences.clearPasswordHash();
					UserPreferences.clearTokenHash();
					tfPassword.clear();
					tfToken.clear();
					option = BUILD_TOKEN_ONLY;
					ivCheckMark.setVisible(false);
				}
				else {
					skip   = true;
					option = BUILD_PASSWORD_ONLY;
					ivCheckMark.setVisible(true);
					cbSave.setSelected(true);//This will trigger this listener again, but the skip boolean will cause it to dump out.
				}
			}
			else {
				if (newValue) {
					ivCheckMark.setVisible(true);
					option = BUILD_BOTH;
				}
				else {
					ivCheckMark.setVisible(false);
					option = BUILD_TOKEN_ONLY;
				}
			}
			switch (option) {
				case BUILD_BOTH -> {
					ivBackBoth.setVisible(true);
					ivBackToken.setVisible(false);
					ivBackPassword.setVisible(false);
					tfToken.setVisible(true);
					tfPassword.setVisible(true);
					tfToken.setPromptText("Personal Access Token");
					tfPassword.setPromptText("New Password (enter)");
				}
				case BUILD_TOKEN_ONLY -> {
					ivBackBoth.setVisible(false);
					ivBackToken.setVisible(true);
					ivBackPassword.setVisible(false);
					tfToken.setVisible(true);
					tfPassword.setVisible(false);
					tfToken.setPromptText("Paste Token (enter)");
				}
				case BUILD_PASSWORD_ONLY -> {
					ivBackBoth.setVisible(false);
					ivBackToken.setVisible(false);
					ivBackPassword.setVisible(true);
					tfToken.setVisible(false);
					tfPassword.setVisible(true);
					tfPassword.setPromptText("Type Password (enter)");
				}
			}
		});
		if (precheckState == HAS_LOCAL_CREDS) {
			ivCheckMark.setVisible(true);
			ivBackBoth.setVisible(false);
			ivBackToken.setVisible(false);
			ivBackPassword.setVisible(true);
			tfToken.setVisible(false);
			tfPassword.setVisible(true);
			tfPassword.setPromptText("Type Password (enter)");
		}
		else {
			ivCheckMark.setVisible(false);
			ivBackBoth.setVisible(false);
			ivBackToken.setVisible(true);
			ivBackPassword.setVisible(false);
			tfToken.setVisible(true);
			tfPassword.setVisible(false);
			tfToken.setPromptText("Paste Token (enter)");
		}
		cbSave.setSelected(currentSecurityMode == PASSWORD_LOGIN || currentSecurityMode == UNKNOWN);
		Scener.showScene(sceneId);
	}

	private void addAPNode(Node node, double left, double right, double top, double bottom) {
		ap.getChildren().add(node);
		if (top != -1) setTopAnchor(node, top);
		if (bottom != -1) setBottomAnchor(node, bottom);
		if (left != -1) setLeftAnchor(node, left);
		if (right != -1) setRightAnchor(node, right);
	}

	private LoginStates preCheck() {
		LoginStates loginStates;
		setBooleans();
		if (hasHashedPassword && hasHashedToken) {loginStates = HAS_LOCAL_CREDS;}
		else {loginStates = NO_LOCAL_CREDS;}
		return loginStates;
	}

	private LoginStates postCheck() {
		LoginStates loginStates;
		setBooleans();
		if (saveToken) {
			if (illegalPassword) {
				return ILLEGAL_PASSWORD;
			}
			if (hasHashedPassword && hasHashedToken && hasTypedPassword) {
				if (checkPassword()) {
					checkToken(false);
					if (tokenValid) {
						return ALL_CREDS_VALID;
					}
					else {
						return TOKEN_FAILURE;
					}
				}
				else {
					passwordAttempts++;
					loginStates = WRONG_PASSWORD_ENTERED;
					if (passwordAttempts > 5) {
						UserPreferences.clearPasswordHash();
						UserPreferences.clearTokenHash();
						loginStates = TOO_MANY_PASSWORD_ATTEMPTS;
					}
					return loginStates;
				}
			}
			else if (noHashedPassword && noHashedToken && hasTypedPassword && hasTypedToken) {
				//Handle the creation of a new local password with an access token.
				checkToken(false);
				if (tokenValid) {
					Response response = new PasswordDialog().ConfirmPasswordYesNoCancel(loginPassword.getValue(), Scener.getOwner(sceneId));
					if (response == Response.YES) {
						new Thread(() -> {
							addInfo("Hashing Password");
							String passwordHash = Crypto.hashPassword(loginPassword.getValue());
							UserPreferences.setPasswordHash(passwordHash);
							UserPreferences.setHashedToken(Crypto.encryptWithSessionKey(userToken.getValue()));
						}).start();
						return HASHING_NEW_PASSWORD;
					}
					else if (response == Response.NO) {return PASSWORD_MISMATCH;}
					else {return USER_CANCELED_CONFIRM_PASSWORD;}
				}
				else {return TOKEN_FAILURE;}
			}
			else if (noHashedToken && noTypedToken) {return NEED_TOKEN;}
			else if (noHashedPassword && noTypedPassword) {return NEED_PASSWORD;}
			else if (noTypedPassword && noTypedToken) {return NO_CREDS_GIVEN;}
			else if (noTypedPassword) {return NEED_PASSWORD;}
			else if (noTypedToken) return NEED_TOKEN;
		}
		else if (noTypedToken) {
			UserPreferences.clearPasswordHash();
			UserPreferences.clearTokenHash();
			return NEED_TOKEN;
		}
		else if (hasTypedToken) {
			UserPreferences.clearTokenHash();
			UserPreferences.clearPasswordHash();
			checkToken(true);
			return tokenValid ? TOKEN_VALID : TOKEN_FAILURE;
		}
		return AMBIGUOUS;
	}

	private void setBooleans() {
		hashedPassword    = UserPreferences.getHashedPassword();
		hashedAccessToken = UserPreferences.getHashedToken();
		hasHashedPassword = hashedPassword.length() != 0;
		hasHashedToken    = hashedAccessToken.length() != 0;
		noHashedToken     = !hasHashedToken;
		noHashedPassword  = !hasHashedPassword;
		saveToken         = saved.getValue().equals(true);
		illegalPassword   = loginPassword.getValue().length() <= 5;
		noTypedPassword   = loginPassword.getValue().isEmpty();
		hasTypedToken     = userToken.getValue().length() > 20;
		hasTypedPassword  = loginPassword.getValue().length() > 5;
		noTypedToken      = userToken.getValue().isEmpty();
		tokenChecked      = false;
	}

	private void checkToken(boolean tokenOnly) {
		if (!tokenChecked) {
			if (userToken.getValue().length() > 20) {
				tokenValid = Action.tokenValid(userToken.getValue());
				Platform.runLater(() -> lblLoggedIn.setText(tokenValid ? "Token Valid" : "Token NOT Valid"));
				addInfo(tokenValid ? "Token Valid" : "Token NOT Valid");
				if (tokenValid && tokenOnly) {
					newSecurityMode = TOKEN_LOGIN;
					Crypto.setSessionKey("");
				}
				tokenChecked = true;
			}
		}
		tfToken.setDisable(false);
		tfPassword.setDisable(false);
	}

	private boolean checkPassword() {
		boolean valid = Crypto.validatePassword(loginPassword.getValue(), hashedPassword);
		if (valid) {
			addInfo("Password Valid");
			newSecurityMode = PASSWORD_LOGIN;
			Crypto.setSessionKey(tfPassword.getText());
			userToken.setValue(Crypto.decryptWithSessionKey(hashedAccessToken));
		}
		tfToken.setDisable(false);
		tfPassword.setDisable(false);
		return valid;
	}

	private void processCredentials() {
		new Thread(() -> {
			if (usingLocalCreds) {
				addInfo("Checking Password...");
			}
			else if (!tfToken.getText().isEmpty() && tfPassword.getText().length() > 5) {
				addInfo("Validating Credentials...");
			}
			postState = postCheck();
			switch (postState) {
				case ILLEGAL_PASSWORD -> {
					CustomAlert.showWarning("Password Invalid", "Password must be more than 5 characters long.");
					tfPassword.clear();
					buttonLogin.setDisable(false);
				}
				case PASSWORD_MISMATCH -> {
					CustomAlert.showWarning("Password Mismatch", "Your passwords did not match, please try again.");
					tfPassword.selectAll();
					buttonLogin.setDisable(false);
				}

				case WRONG_PASSWORD_ENTERED -> {
					CustomAlert.showWarning("Incorrect Password", "The password you entered is incorrect. After six failed attempts, GistFX will reset your password and access token so that you can create a new password with a working access token.\n\nYou have " + (6 - passwordAttempts) + " attempts remaining.");
					buttonLogin.setDisable(false);
					addInfo("clear");
				}

				case ALL_CREDS_VALID -> new Thread(this::logIntoGitHub).start();

				case TOO_MANY_PASSWORD_ATTEMPTS -> Platform.runLater(() -> {
					tooManyPasswords = true;
					CustomAlert.showRequireOK("Mandatory Reset", "You have entered the wrong password too many times. Your Gist access token and password have both been reset. You can enter a new (or the same) access token and type in a new password.\n\nThe program will now close. Please re-open it to continue.", Scener.getOwner(sceneId));
					System.exit(241);
					tfPassword.clear();
					tfToken.clear();
					tfToken.requestFocus();
					cbSave.setSelected(false);
					saved.bind(cbSave.selectedProperty());
					buildScene(BUILD_TOKEN_ONLY);
					tooManyPasswords = false;
				});

				case HASHING_NEW_PASSWORD -> {
					newSecurityMode = PASSWORD_LOGIN;
					Crypto.setSessionKey(tfPassword.getText());
					new Thread(this::logIntoGitHub).start();
				}

				case TOKEN_VALID -> new Thread(this::logIntoGitHub).start();

				case TOKEN_FAILURE -> CustomAlert.showWarning("Token Failure", "Your token could not authenticate.\n\nPlease click on the question mark for more information.");

				case NO_CREDS_GIVEN -> CustomAlert.showWarning("Need Credentials", "You need to provide the required information.");

				case USER_CANCELED_CONFIRM_PASSWORD -> tfPassword.selectAll();

				case NEED_TOKEN -> {
					CustomAlert.showInfo("Need Token", "You did not provide an access token.", Scener.getOwner(sceneId));
					tfToken.requestFocus();
				}

				case NEED_PASSWORD -> {
					CustomAlert.showInfo("Need Password", "You did not provide a password.", Scener.getOwner(sceneId));
					tfPassword.requestFocus();
				}

				case AMBIGUOUS -> CustomAlert.showInfo("Ambiguous", "This isn't really an error, it's just a default when everything else has run its course. I don't expect anyone to ever see this alert.", Scener.getOwner(sceneId));
			}
		}).start();
		buttonLogin.setDisable(true); //Lock button to prevent double clicking
	}

	private void logIntoGitHub() {
		LiveSettings.applyUserPreferences();
		if (!currentSecurityMode.equals(newSecurityMode) || passwordChanged) {
			/*
			 * This ensures that if the user switches between password login and
			 * token login, the encryption key will be changed and the data needs
			 * to be re-encrypted.
			 */
			Action.cleanDatabase();
			LiveSettings.dataSource = UISettings.DataSource.GITHUB;
			UserPreferences.setSecurityOption(newSecurityMode);
		}
		Action.loadData();
		Platform.runLater(() -> pBar.progressProperty().unbind());
		tfPassword.setText(Crypto.randomText(255));
		tfToken.setText(Crypto.randomText(255));
	}

}
