import Data.DirectMessage;
import Data.Group;
import Data.GroupMessage;
import Data.User;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

public class HomeController implements CustomController, Initializable {
    public VBox root;
    public TextField messageEntryField;
    public Button sendButton;
    public ListView<String> listView;

    public void onMessageEntryKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendButtonPressed(new ActionEvent());
        }
    }

    public void sendButtonPressed(ActionEvent actionEvent) {
        String message = messageEntryField.getText();
        if (GUIClient.clientConnection.dataManager.isValidGroup(GUIClient.currentActiveChat)) {
            GroupMessage m = new GroupMessage();
            m.message.content = message;
            m.message.sender = GUIClient.clientConnection.uuid;
            m.receivingGroup = GUIClient.currentActiveChat;
            GUIClient.clientConnection.send(new Packet(m));
        } else if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
            DirectMessage m = new DirectMessage();
            m.message.content = message;
            m.message.sender = GUIClient.clientConnection.uuid;
            m.receiver = GUIClient.currentActiveChat;
            GUIClient.clientConnection.send(new Packet(m));
        }
        messageEntryField.clear();
    }

    public void listItemClicked(MouseEvent mouseEvent) {
        String selectedItem = listView.getSelectionModel().getSelectedItem();
        System.out.printf("List item " + selectedItem + " was clicked");
    }

    private void onLoginSuccess() {
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
        GUIClient.currentActiveChat = GUIClient.clientConnection.dataManager.getGlobalGroup();
    }

    private void refreshGUI() {
        String username = GUIClient.clientConnection.dataManager.users.get(GUIClient.clientConnection.uuid).username;
        if (username != null) {
            GUIClient.primaryStage.setTitle("Logged in as \"" + username + "\"");
        }

        listView.getItems().clear();
        listView.getItems().add("Groups:");
        for (Map.Entry<UUID, Group> pair : GUIClient.clientConnection.dataManager.groups.entrySet()) {
            listView.getItems().add(pair.getValue().toString());
        }
        listView.getItems().add("Users:");
        for (Map.Entry<UUID, User> pair : GUIClient.clientConnection.dataManager.users.entrySet()) {
            listView.getItems().add(pair.getValue().toString());
        }
        int id = 1;
        HashMap<UUID, Integer> disconnectedUsers = new HashMap<UUID, Integer>();
        listView.getItems().add("Messages:");
        if (GUIClient.clientConnection.dataManager.isValidGroup(GUIClient.currentActiveChat)) {
            for (Data.Message m : GUIClient.clientConnection.dataManager.getGroupChat(GUIClient.currentActiveChat).messages) {
                String senderName = "Disconnected User(";
                if (GUIClient.clientConnection.dataManager.isValidUser(m.sender)) {
                    senderName = GUIClient.clientConnection.dataManager.users.get(m.sender).username;
                } else if (disconnectedUsers.containsKey(m.sender)){
                    senderName += disconnectedUsers.get(m.sender) + ")";
                } else {
                    senderName += id + ")";
                    disconnectedUsers.put(m.sender, id);
                    id++;
                }
                listView.getItems().add(senderName + ": " + m.content);
            }
        }
        else if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
            for (Data.Message m : GUIClient.clientConnection.dataManager.getDM(GUIClient.clientConnection.uuid, GUIClient.currentActiveChat).messages) {
                String senderName = "Disconnected User(";
                if (GUIClient.clientConnection.dataManager.isValidUser(m.sender)) {
                    senderName = GUIClient.clientConnection.dataManager.users.get(m.sender).username;
                } else if (disconnectedUsers.containsKey(m.sender)){
                    senderName += disconnectedUsers.get(m.sender) + ")";
                } else {
                    senderName += id + ")";
                    disconnectedUsers.put(m.sender, id);
                    id++;
                }
                listView.getItems().add(senderName + ": " + m.content);
            }
        }
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case LOGIN_SUCCESS:
                onLoginSuccess();
                break;
            case REFRESH:
                refreshGUI();
                break;
            case LOGIN_ERROR:
            default:
                break;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("home", new GUIView(null, this));
    }
}
