import Data.GroupCreate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

public class CreateGroupController implements CustomController, Initializable {
    public Label errorIndicator;
    public TextField usernameEntryField;
    public CheckBox privateCheckbox;
    public CheckBox allowInvitesCheckbox;
    public ListView<String> userSelector;
    private final ArrayList<UUID> selectedUsers = new ArrayList<>();

    public void onNameEntryKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            createButtonPressed(new ActionEvent());
        }
    }

    public void privateCheckboxToggled(ActionEvent actionEvent) {
        boolean isPrivate = privateCheckbox.isSelected();
        if (isPrivate) {
            userSelector.setDisable(false);
            allowInvitesCheckbox.setDisable(false);
        } else {
            userSelector.setDisable(true);
            allowInvitesCheckbox.setDisable(true);
        }
    }

    public void allowInvitesCheckboxToggled(ActionEvent actionEvent) {
    }

    public void createButtonPressed(ActionEvent actionEvent) {
        String groupName = usernameEntryField.getText();
        if (!groupName.isEmpty()) {
            GroupCreate d = new GroupCreate();
            d.creatorID = GUIClient.clientConnection.uuid;
            d.groupName = usernameEntryField.getText();
            boolean isPrivate = privateCheckbox.isSelected();
            if (isPrivate) {
                d.isPrivate = isPrivate;
                d.allowInvites = privateCheckbox.isSelected();
            } else {
                d.isPrivate = false;
                d.allowInvites = true;
            }
        }
    }

    public void onGroupCreateError() {
        errorIndicator.setVisible(true);
    }

    public void onGroupCreateSuccess() {
        errorIndicator.setVisible(false);
    }

    public void onRefresh() {
        userSelector.getItems().clear();
        GUIClient.clientConnection.dataManager.users.forEach((k, v) -> {
            if (v.username != null && !v.uuid.equals(GUIClient.clientConnection.uuid)) {
                userSelector.getItems().add(v.username);
            }
        });
        ArrayList<UUID> idsToRemove = new ArrayList<UUID>();
        for (UUID id : selectedUsers) {
            if (!GUIClient.clientConnection.dataManager.isValidUser(id)) {
                idsToRemove.add(id);
            }
        }
        for (UUID id : idsToRemove) {
            selectedUsers.remove(id);
        }
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case GROUP_CREATE_ERROR:
                onGroupCreateError();
            case GROUP_CREATE_SUCCESS:
                onGroupCreateSuccess();
            case REFRESH:
                onRefresh();
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
        GUIClient.viewMap.put("createGroup", new GUIView(null, this));
        userSelector.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        selectedUsers.add(GUIClient.clientConnection.dataManager.getByUsername(item));
                    } else if (wasSelected) {
                        selectedUsers.remove(GUIClient.clientConnection.dataManager.getByUsername(item));
                    }
                });
                return observable;
            }
        }));
    }
}
