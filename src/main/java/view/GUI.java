package view;

import controller.Controller;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

import javafx.stage.Stage;
import javafx.fxml.FXML;

import java.io.File;
public class GUI {

    Controller controller = new Controller();
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField pathForCorpusAndStopWords;
    @FXML
    private TextField pathForDictionaryAndPosting;
    @FXML
    private Button browse1;
    @FXML
    private Button browse2;
    @FXML
    private javafx.scene.control.CheckBox stemming;
    @FXML
    private ComboBox languages;
    @FXML
    private Button start;
    @FXML
    private Button reset;
    @FXML
    private Button showDic;
    @FXML
    private Button loadDic;


    public void browse1(){
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(new Stage());
        if (file == null){
            showErrorAlert("No directory selected.");
        }
        else {
            pathForCorpusAndStopWords.setText(file.getAbsolutePath());
        }
    }

    public void browse2(){
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(new Stage());
        if (file == null){
            showErrorAlert("No directory selected.");
        }
        else {
            pathForDictionaryAndPosting.setText(file.getAbsolutePath());
        }
    }

    public CheckBox getStemming() {
        return stemming;

    }

    public TextField getPathForCorpusAndStopWords() {
        return pathForCorpusAndStopWords;
    }

    public TextField getPathForDictionaryAndPosting() {
        return pathForDictionaryAndPosting;
    }

    protected void showInformationAlert(String stringAlert){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Message");
        alert.setContentText(stringAlert);
        alert.show();
    }

    protected void showErrorAlert(String stringAlert){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Message");
        alert.setContentText(stringAlert);
        alert.showAndWait();
    }

}
