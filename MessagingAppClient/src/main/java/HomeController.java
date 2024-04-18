import Data.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    public TextField messageEntryField;
    public Button sendButton;
    public Label groupsLabel;
    public Label chatNameIndicator;
    public Button createGroupButton;
    public ListView<String> contentView;
    public ListView<String> userDisplay;
    public ListView<String> groupDisplay;

    public void groupDisplayClicked(MouseEvent mouseEvent) {
        String selectedItem = groupDisplay.getSelectionModel().getSelectedItem();
        System.out.println("List item " + selectedItem + " was clicked");
        GUIClient.currentActiveChat = GUIClient.clientConnection.dataManager.getByGroupName(selectedItem);
        updateUI(new GUICommand(GUICommand.Type.REFRESH));
    }
    public void userDisplayClicked(MouseEvent mouseEvent) {
        String selectedItem = userDisplay.getSelectionModel().getSelectedItem();
        System.out.println("List item " + selectedItem + " was clicked");
        UUID selected = GUIClient.clientConnection.dataManager.getByUsername(selectedItem);
        System.out.println("Client connection is " + GUIClient.clientConnection.uuid);
        System.out.println("Selected is " + selected);
        if (!selected.equals(GUIClient.clientConnection.uuid)) {
            GUIClient.currentActiveChat = selected;
        }
        updateUI(new GUICommand(GUICommand.Type.REFRESH));
    }
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

    public void createGroupButtonPressed(ActionEvent actionEvent) {
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("createGroup").scene);
    }

    private void onLoginSuccess() {
        GUIClient.clientConnection.lastOperation = Payload.Type.GROUP_CREATE;
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
        GUIClient.currentActiveChat = GUIClient.clientConnection.dataManager.getGlobalGroup();
    }

    private void refreshGUI() {
        if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
            chatNameIndicator.setText("DM with \"" + GUIClient.clientConnection.dataManager.users.get(GUIClient.currentActiveChat).username + "\"");
        } else if (GUIClient.currentActiveChat != null) {
            chatNameIndicator.setText("Group \"" + GUIClient.clientConnection.dataManager.groups.get(GUIClient.currentActiveChat).name + "\"");
        }
        String username = GUIClient.clientConnection.dataManager.users.get(GUIClient.clientConnection.uuid).username;
        if (username != null) {
            GUIClient.primaryStage.setTitle("Logged in as \"" + username + "\"");
        }

        groupDisplay.getItems().clear();
        for (Map.Entry<UUID, Group> pair : GUIClient.clientConnection.dataManager.groups.entrySet()) {
            groupDisplay.getItems().add(pair.getValue().toDisplayString());
        }

        boolean didAddAny = false;
        userDisplay.getItems().clear();
        for (Map.Entry<UUID, User> pair : GUIClient.clientConnection.dataManager.users.entrySet()) {
            if ((!pair.getKey().equals(GUIClient.clientConnection.uuid)) && (pair.getValue().username != null) && (!pair.getValue().username.equals("Server"))) {
                userDisplay.getItems().add(pair.getValue().toDisplayString());
                didAddAny = true;
            }
        }
        if (!didAddAny) {
            userDisplay.getItems().add("");
        }


        contentView.getItems().clear();
        int id = 1;
        HashMap<UUID, Integer> disconnectedUsers = new HashMap<UUID, Integer>();
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
                if (senderName.equals("Server")) {
                    contentView.getItems().add(m.content);
                } else {
                    contentView.getItems().add(senderName + ": " + m.content);
                }
            }
        }
        else if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
            if (GUIClient.clientConnection.dataManager.getDM(GUIClient.clientConnection.uuid, GUIClient.currentActiveChat) != null) {
                for (Data.Message m : GUIClient.clientConnection.dataManager.getDM(GUIClient.clientConnection.uuid, GUIClient.currentActiveChat).messages) {
                    String senderName = "Disconnected User(";
                    if (GUIClient.clientConnection.dataManager.isValidUser(m.sender)) {
                        senderName = GUIClient.clientConnection.dataManager.users.get(m.sender).username;
                    } else if (disconnectedUsers.containsKey(m.sender)) {
                        senderName += disconnectedUsers.get(m.sender) + ")";
                    } else {
                        senderName += id + ")";
                        disconnectedUsers.put(m.sender, id);
                        id++;
                    }
                    if (senderName.equals("Server")) {
                        contentView.getItems().add(m.content);
                    } else {
                        contentView.getItems().add(senderName + ": " + m.content);
                    }
                }
            }
        }
        else {
            contentView.getItems().add("");
        }
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case LOGIN_SUCCESS:
                onLoginSuccess();
                break;
            case REFRESH:
            case GROUP_CREATE_SUCCESS:
                refreshGUI();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResizeWidth(Number oldVal, Number newVal) {

    }

    @Override
    public void onResizeHeight(Number oldVal, Number newVal) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("home", new GUIView(null, this));
    }
}
