
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Data.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIClient extends Application {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    HashMap<String, Scene> sceneMap;
    Client clientConnection;

    ListView<String> listItems2;

    boolean isDMActive = false;
    UUID currentActiveChat = null;
    Stage primaryStage = null;
    Label invalidUsernameLabel = null;


    public static void main(String[] args) {
        launch(args);
    }

    private void sendMessageToActiveChat(String message) {
        System.out.println("Sending message to " + currentActiveChat.toString());
        Packet p;
        if (!isDMActive) {
            GroupMessage m = new GroupMessage();
            m.message.content = message;
            m.message.sender = clientConnection.uuid;
            m.receivingGroup = currentActiveChat;
            p = new Packet(m);
        } else {
            DirectMessage m = new DirectMessage();
            m.message.content = message;
            m.message.sender = clientConnection.uuid;
            m.receiver = currentActiveChat;
            p = new Packet(m);
        }
        clientConnection.send(p);
    }

    private void loginToServer(String username) {
        System.out.println("Attempting login to server");
        Packet p;
        LoginAttempt m = new LoginAttempt();
        m.username = username;
        p = new Packet(m);
        clientConnection.send(p);
    }

    private void requestRefresh() {

    }

    private void refreshGUI() {
        System.out.println("Refreshing GUI");
        listItems2.getItems().clear();
        listItems2.getItems().add("Groups:");
        for (Map.Entry<UUID, Group> pair : clientConnection.dataManager.groups.entrySet()) {
            listItems2.getItems().add(pair.getValue().toString());
        }
        listItems2.getItems().add("Users:");
        for (Map.Entry<UUID, User> pair : clientConnection.dataManager.users.entrySet()) {
            listItems2.getItems().add(pair.getValue().toString());
        }
    }

    private void onLoginError() {
        invalidUsernameLabel.setVisible(true);
    }

    private void onLoginSuccess() {
        primaryStage.setTitle(clientConnection.dataManager.users.get(clientConnection.uuid).username);
        primaryStage.setScene(sceneMap.get("client"));
        currentActiveChat = clientConnection.dataManager.getGlobalGroup();
    }

    @Override
    public void start(Stage pStage) throws Exception {
        primaryStage = pStage;
        clientConnection = new Client(data -> {
            Platform.runLater(() -> {
                GUICommand c = (GUICommand) data;
                switch (c.type) {
                    case REFRESH: {
                        refreshGUI();
                        break;
                    }
                    case LOGIN_ERROR: {
                        onLoginError();
                        break;
                    }
                    case LOGIN_SUCCESS: {
                        onLoginSuccess();
                        break;
                    }
                    default: {
                        break;
                    }
                }

            });
        });

        clientConnection.start();

        sceneMap = new HashMap<String, Scene>();

        sceneMap.put("login", createLoginGUI());
        sceneMap.put("client", createClientGUI());

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });


        primaryStage.setScene(sceneMap.get("login"));
        primaryStage.setTitle("Client");
        primaryStage.show();

    }

    public Scene createLoginGUI() {
        invalidUsernameLabel = new Label("Username Taken");
        invalidUsernameLabel.setAlignment(Pos.CENTER);
        invalidUsernameLabel.setVisible(false);
        invalidUsernameLabel.setTextFill(Color.RED);

        Label entryBoxLabel = new Label("Username: ");
        entryBoxLabel.setTextFill(Color.WHITE);
        TextField usernameEntryField = new TextField("");
        HBox nameEntryBox = new HBox(10, entryBoxLabel, usernameEntryField);
        nameEntryBox.setAlignment(Pos.CENTER);
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            loginToServer(usernameEntryField.getText());
        });
        loginButton.setAlignment(Pos.CENTER);

        VBox mainVB = new VBox(10, invalidUsernameLabel, nameEntryBox, loginButton);
        mainVB.setAlignment(Pos.CENTER);
        mainVB.setStyle("-fx-background-color: blue;" + "-fx-font-family: 'serif';");
        return new Scene(mainVB, WIDTH, HEIGHT);
    }

    public Scene createClientGUI() {
        listItems2 = new ListView<String>();

        TextField c1 = new TextField();
        Button b1 = new Button("Send");
        b1.setOnAction(e -> {
            sendMessageToActiveChat(c1.getText());
            c1.clear();
        });
        Button b2 = new Button("Refresh");
        b2.setOnAction(e -> {
            Platform.runLater(() -> {
                refreshGUI();
            });
        });

        VBox clientBox = new VBox(10, c1, b1, b2, listItems2);
        clientBox.setStyle("-fx-background-color: blue;" + "-fx-font-family: 'serif';");
        return new Scene(clientBox, WIDTH, HEIGHT);
    }
}
