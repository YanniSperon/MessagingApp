
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Data.Group;
import Data.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIServer extends Application {

    HashMap<String, Scene> sceneMap;
    public static Server serverConnection;

    ListView<String> listItems, listItems2, listItems3;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        serverConnection = new Server(data -> {
            Platform.runLater(() -> {
                listItems.getItems().add(data.toString());
                listItems2.getItems().clear();
                listItems2.getItems().add("Users:");
                for (Map.Entry<UUID, User> pair : serverConnection.dataManager.users.entrySet()) {
                    listItems2.getItems().add(pair.getValue().toString());
                }
                listItems3.getItems().clear();
                listItems3.getItems().add("Groups:");
                for (Map.Entry<UUID, Group> pair : serverConnection.dataManager.groups.entrySet()) {
                    listItems3.getItems().add(pair.getValue().toString());
                }
            });
        });


        listItems = new ListView<String>();
        listItems2 = new ListView<String>();
        listItems3 = new ListView<String>();

        sceneMap = new HashMap<String, Scene>();

        sceneMap.put("server", createServerGUI());

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.setScene(sceneMap.get("server"));
        primaryStage.setTitle("Server");
        primaryStage.show();

    }

    public Scene createServerGUI() {

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(70));
        pane.setStyle("-fx-background-color: coral");

        pane.setCenter(listItems);
        pane.setLeft(listItems2);
        pane.setRight(listItems3);
        pane.setStyle("-fx-font-family: 'serif'");
        return new Scene(pane, 800, 400);


    }


}
