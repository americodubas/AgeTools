package agetools;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Database;
import util.DatabaseHelperKt;
import util.Toast;

import java.util.List;

import static util.DatabaseHelperKt.isNameAlreadyUsed;

public class AgeToolsController {

    @FXML
    private ListView<String> databaseList;

    @FXML
    public Button btCreate;

    @FXML
    public Button btUpdate;

    @FXML
    public Button btDelete;

    @FXML
    public Button btChange;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfUser;

    @FXML
    private TextField tfUrl;

    private int id;

    private int selectedIndex;

    /**
     * Initializes the controller class.
     */
    @FXML
    void initialize() {
        ObservableList<String> obs = FXCollections.observableArrayList();
        obs.addAll(DatabaseHelperKt.getAllDatabasesNames());
        databaseList.setItems(obs);
        databaseList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        databaseList.getSelectionModel().selectedItemProperty().addListener(databaseListener);
        databaseList.getSelectionModel().selectFirst();
        blockDeleteButton();
    }

    @FXML
    public void create(ActionEvent actionEvent) {
        databaseList.getItems().add(DatabaseHelperKt.createDatabase().getName());
        blockDeleteButton();
    }

    private final ChangeListener<String> databaseListener = (observable, oldValue, newValue) -> databaseSelected(newValue);

    private void databaseSelected(String newValue) {
        if (newValue == null) {
            return;
        }
        Database database = DatabaseHelperKt.getDatabaseBy(newValue);
        if (database != null) {
            selectedIndex = databaseList.getSelectionModel().getSelectedIndex();
            id = database.getId();
            tfName.setText(database.getName());
            tfUser.setText(database.getUser());
            tfUrl.setText(database.getUrl());
        }
    }

    @FXML
    public void update(ActionEvent actionEvent) {
        if (isMissingRequiredFields(actionEvent)){
            return;
        }
        if (isNameAlreadyUsed(tfName.getText(), id)) {
            Toast.makeText(getStage(actionEvent),"Name already used!");
            return;
        }
        Database database = DatabaseHelperKt.getDatabaseBy(id);
        if (database != null) {
            database.setName(tfName.getText());
            database.setUser(tfUser.getText());
            database.setUrl(tfUrl.getText());
            DatabaseHelperKt.updateDatabase(database);
        }
        databaseList.getItems().set(selectedIndex, tfName.getText());
    }

    @FXML
    public void delete(ActionEvent actionEvent) {
        if (canDelete(actionEvent)) {
            DatabaseHelperKt.deleteDatabaseBy(id);
            databaseList.getItems().remove(selectedIndex);
        } else {
            Toast.makeText(getStage(actionEvent), "Can't delete last database!");
        }
        blockDeleteButton();
        databaseList.getSelectionModel().selectFirst();
    }

    @FXML
    public void change(ActionEvent actionEvent) {
        DatabaseHelperKt.changeConnectionTo(id);
        Toast.makeText(getStage(actionEvent), "Database changed to " + databaseList.getSelectionModel().getSelectedItem());
    }

    private void blockDeleteButton() {
        btDelete.setDisable(databaseList.getItems().size() == 1);
    }

    private boolean canDelete(ActionEvent actionEvent) {
        return databaseList.getItems().size() > 1;
    }

    private Stage getStage(ActionEvent actionEvent) {
        return (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
    }

    private boolean isMissingRequiredFields(ActionEvent actionEvent) {
        boolean missing = false;
        if (tfName.getText().trim().length() == 0) {
            Toast.makeText(getStage(actionEvent), "Name is required!");
            missing = true;
        }
        if (!missing && tfUser.getText().trim().length() == 0) {
            Toast.makeText(getStage(actionEvent), "User is required!");
            missing = true;
        }
        if (!missing && tfUrl.getText().trim().length() == 0) {
            Toast.makeText(getStage(actionEvent), "URL is required!");
            missing = true;
        }

        return missing;
    }
}
