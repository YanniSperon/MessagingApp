import Data.LoginAttempt;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements CustomController, Initializable {
    public Label invalidUsernameIndicator;
    public TextField usernameEntryField;
    public Button loginButton;

    public void loginButtonPressed(ActionEvent actionEvent) {
        System.out.println("Attempting login to server");

        LoginAttempt m = new LoginAttempt();
        m.username = usernameEntryField.getText();

        GUIClient.clientConnection.send(new Packet(m));
    }

    public void onUsernameEntryKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            loginButtonPressed(new ActionEvent());
        }
    }

    public void onInvalidLogin() {
        invalidUsernameIndicator.setVisible(true);
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case LOGIN_ERROR:
                onInvalidLogin();
                break;
            case LOGIN_SUCCESS:
            case REFRESH:
            default:
                break;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("login", new GUIView(null, this));
    }
}
