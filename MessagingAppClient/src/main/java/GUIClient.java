
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Data.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIClient extends Application {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    public static HashMap<String, GUIView> viewMap;
    public static Client clientConnection;

    public static UUID currentActiveChat = null;
    public static Stage primaryStage = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) throws Exception {
        primaryStage = pStage;
        clientConnection = new Client(data -> {
            Platform.runLater(() -> {
                GUICommand c = (GUICommand) data;
                viewMap.forEach((k, v) -> {
                    v.controller.updateUI(c);
                });
            });
        });

        viewMap = new HashMap<String, GUIView>();

        clientConnection.start();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        createLoginGUI();
        createHomeGUI();

        primaryStage.setScene(viewMap.get("login").scene);
        primaryStage.setTitle("Not logged in");
        primaryStage.show();

    }

    public void createLoginGUI() {
        //invalidUsernameLabel = new Label("Username Taken");
        //invalidUsernameLabel.setAlignment(Pos.CENTER);
        //invalidUsernameLabel.setVisible(false);
        //invalidUsernameLabel.setTextFill(Color.RED);
        //
        //Label entryBoxLabel = new Label("Username: ");
        //entryBoxLabel.setTextFill(Color.WHITE);
        //TextField usernameEntryField = new TextField("");
        //HBox nameEntryBox = new HBox(10, entryBoxLabel, usernameEntryField);
        //nameEntryBox.setAlignment(Pos.CENTER);
        //Button loginButton = new Button("Login");
        //loginButton.setOnAction(e -> {
        //    loginToServer(usernameEntryField.getText());
        //});
        //loginButton.setAlignment(Pos.CENTER);
        //
        //VBox mainVB = new VBox(10, invalidUsernameLabel, nameEntryBox, loginButton);
        //mainVB.setAlignment(Pos.CENTER);
        //mainVB.setStyle("-fx-background-color: blue;" + "-fx-font-family: 'serif';");
        //return new Scene(mainVB, WIDTH, HEIGHT);
        try {
            // Read file fxml and draw interface.
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/login.fxml"));
            Scene s1 = new Scene(root, WIDTH, HEIGHT);
            s1.getStylesheets().add("/styles/login.css");
            viewMap.get("login").scene = s1;
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }

    public void createHomeGUI() {
        try {
            // Read file fxml and draw interface.
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/home.fxml"));
            Scene s1 = new Scene(root, WIDTH, HEIGHT);
            s1.getStylesheets().add("/styles/home.css");
            viewMap.get("home").scene = s1;
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }
}
