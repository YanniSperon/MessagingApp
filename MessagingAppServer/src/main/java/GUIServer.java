
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Data.Group;
import Data.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIServer extends Application {

    HashMap<String, Scene> sceneMap;
    public static Server serverConnection;

    ListView<String> listItems, listItems2, listItems3, listItems4;

    Label activeChatLabel;
    UUID activeChat;
    UUID activeChat2;

    public static void main(String[] args) {
        launch(args);
    }

    private void updateChat() {
        listItems4.getItems().clear();
        int id = 1;
        HashMap<UUID, Integer> disconnectedUsers = new HashMap<UUID, Integer>();
        if (serverConnection.dataManager.isValidGroup(activeChat)) {
            activeChatLabel.setText("Group \"" + serverConnection.dataManager.groups.get(activeChat).name + "\"");
            for (Data.Message m : serverConnection.dataManager.getGroupChat(activeChat).messages) {
                String senderName = "Disconnected User(";
                if (serverConnection.dataManager.isValidUser(m.sender)) {
                    senderName = serverConnection.dataManager.users.get(m.sender).username;
                } else if (disconnectedUsers.containsKey(m.sender)) {
                    senderName += disconnectedUsers.get(m.sender) + ")";
                } else {
                    senderName += id + ")";
                    disconnectedUsers.put(m.sender, id);
                    id++;
                }
                if (senderName.equals("Server")) {
                    listItems4.getItems().add(m.content);
                } else {
                    listItems4.getItems().add(senderName + ": " + m.content);
                }
            }
        } else if (serverConnection.dataManager.isValidUser(activeChat)) {
            if (serverConnection.dataManager.isValidUser(activeChat2)) {
                activeChatLabel.setText("DM between \"" + serverConnection.dataManager.users.get(activeChat).username + "\" and \"" + serverConnection.dataManager.users.get(activeChat2).username + "\"");
                for (Data.Message m : serverConnection.dataManager.getDM(activeChat, activeChat2).messages) {
                    String senderName = "Disconnected User(";
                    if (serverConnection.dataManager.isValidUser(m.sender)) {
                        senderName = serverConnection.dataManager.users.get(m.sender).username;
                    } else if (disconnectedUsers.containsKey(m.sender)) {
                        senderName += disconnectedUsers.get(m.sender) + ")";
                    } else {
                        senderName += id + ")";
                        disconnectedUsers.put(m.sender, id);
                        id++;
                    }
                    if (senderName.equals("Server")) {
                        listItems4.getItems().add(m.content);
                    } else {
                        listItems4.getItems().add(senderName + ": " + m.content);
                    }
                }
            } else {
                activeChatLabel.setText("Choose another user to view DM");
            }
        } else {
            listItems4.getItems().add("");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        listItems = new ListView<String>();
        listItems2 = new ListView<String>();
        listItems3 = new ListView<String>();
        listItems4 = new ListView<String>();

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


        serverConnection = new Server(data -> {
            Platform.runLater(() -> {
                listItems.getItems().add(data.toString());
                listItems2.getItems().clear();
                boolean anyAdded = false;
                for (Map.Entry<UUID, User> pair : serverConnection.dataManager.users.entrySet()) {
                    if (pair.getValue().username != null && !pair.getValue().username.equals("Server")) {
                        listItems2.getItems().add(pair.getValue().toDisplayString());
                        anyAdded = true;
                    }
                }
                if (!anyAdded) {
                    listItems2.getItems().add("");
                }
                listItems3.getItems().clear();
                for (Map.Entry<UUID, Group> pair : serverConnection.dataManager.groups.entrySet()) {
                    listItems3.getItems().add(pair.getValue().toDisplayString());
                }
                updateChat();
            });
        });
    }

    public Scene createServerGUI() {

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(70));
        pane.setStyle("-fx-background-color: coral");

        Label centerLLabel = new Label("Log");
        centerLLabel.setAlignment(Pos.CENTER);
        VBox centerLVbox = new VBox(10, centerLLabel, listItems);
        centerLVbox.setAlignment(Pos.CENTER);

        activeChatLabel = new Label("No active chat");
        activeChatLabel.setAlignment(Pos.CENTER);
        VBox centerRVbox = new VBox(10, activeChatLabel, listItems4);
        centerRVbox.setAlignment(Pos.CENTER);

        HBox centerHbox = new HBox(0, centerLVbox, centerRVbox);
        centerHbox.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerLVbox, Priority.ALWAYS);
        HBox.setHgrow(centerRVbox, Priority.ALWAYS);
        centerHbox.setPrefWidth(400);
        pane.setCenter(centerHbox);

        Label rightLabel = new Label("Users (Select two to view DM)");
        rightLabel.setAlignment(Pos.CENTER);
        VBox rightVbox = new VBox(10, rightLabel, listItems2);
        listItems2.setOnMouseClicked((e) -> {
            String selectedItem = listItems2.getSelectionModel().getSelectedItem();
            if (!selectedItem.isEmpty()) {
                if (activeChat != null && activeChat2 != null) {
                    activeChat = serverConnection.dataManager.getByUsername(selectedItem);
                    activeChat2 = null;
                } else if (activeChat != null) {
                    if (serverConnection.dataManager.isValidUser(activeChat)) {
                        activeChat2 = serverConnection.dataManager.getByUsername(selectedItem);
                        if (activeChat.equals(activeChat2)) {
                            activeChat2 = null;
                        }
                    } else {
                        activeChat = serverConnection.dataManager.getByUsername(selectedItem);
                    }
                } else {
                    activeChat = serverConnection.dataManager.getByUsername(selectedItem);
                }
                updateChat();
            }
        });
        rightVbox.setAlignment(Pos.CENTER);
        pane.setRight(rightVbox);

        Label leftLabel = new Label("Groups (Click to view chat)");
        leftLabel.setAlignment(Pos.CENTER);
        VBox leftVbox = new VBox(10, leftLabel, listItems3);
        listItems3.setOnMouseClicked((e) -> {
            String selectedItem = listItems3.getSelectionModel().getSelectedItem();
            System.out.println("Setting active chat as " + selectedItem);
            activeChat = serverConnection.dataManager.getByGroupName(selectedItem);
            System.out.println("Active chat now " + activeChat);
            activeChat2 = null;
            updateChat();
        });
        leftVbox.setAlignment(Pos.CENTER);

        pane.setLeft(leftVbox);
        pane.setStyle("-fx-font-family: 'serif'");
        return new Scene(pane, 1400, 800);


    }


}
