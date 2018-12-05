package view;

import controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gui class - connected to the fxml file.
 */
public class GUI {

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

    private Controller controller;


    public Controller getController() {
        return controller;
    }

    /**
     * first browse function, opens the browse directory option.
     */
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

    /**
     * second browse function, opens the browse directory option.
     */
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

    /**
     * on action for the combo box, writes the chosen string in the combobox.
     * @param event
     */
    public void comboAction(ActionEvent event) {
        String field = (String)languages.getValue();
        if(field == null)
            return;
    }

    /**
     * on action for the start button.
     * show the results from the start button to an information alert.
     */
    public void startClicked(){
        String corpusAndStopWordsPath = this.pathForCorpusAndStopWords.getText();
        String dicPath = this.pathForDictionaryAndPosting.getText();
        File file1 = new File(corpusAndStopWordsPath);
        File file2 = new File(dicPath);
        String stopWordsPath;
        if (file1.isDirectory() && file2.isDirectory()){
            if (corpusAndStopWordsPath.contains("\\")){
                corpusAndStopWordsPath = replaceChars(corpusAndStopWordsPath,"\\", "/");
                stopWordsPath = corpusAndStopWordsPath + "/stop_words.txt";
            }
            else {
                stopWordsPath = corpusAndStopWordsPath + "/stop_words.txt";
            }
            if (dicPath.contains("\\") && dicPath.charAt(dicPath.length()-1) != '\\'){
                dicPath = dicPath+"\\";
            }
            if (dicPath.contains("/") && dicPath.charAt(dicPath.length()-1) != '/'){
                dicPath = dicPath+"/";
            }
            controller = new Controller(corpusAndStopWordsPath, stopWordsPath, dicPath, stemming.isSelected());
            Thread thread = new Thread(controller);
            thread.start();
            try {
                thread.join();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            List<Long> results = controller.getResult();
            showInformationAlert("Inverted Index process ended successfully.\n" +
                                            "Number of documents: "+results.get(0)+".\n"+
                                            "Uniqe terms amount: "+results.get(1)+".\n"+
                                            "Total running is: "+results.get(2)+"(sec).");
        }
        else {
            showErrorAlert("Directory path is not valid, please try again.");
        }
    }

    /**
     * this function shows the dictionary after starting the system, or loading a valid dictionary path.
     */
    public void showDic(){
        initControllerForExistingFiles();
        if (controller != null){
            File file;
            StringBuilder filecontent = new StringBuilder();
            ObservableList<String> observableList = FXCollections.observableArrayList();
            if (stemming.isSelected()){
                file = new File(controller.getDicPath()+"stemmedDictionaryToShow");
            }
            else {
                file = new File(controller.getDicPath()+"dictionaryFileToShow");
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while (line != null){
                    filecontent.append(line+'\n');
                    line = br.readLine();
                    if (line != null)
                        observableList.add(line);
                }
                br.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            ListView<String> list = new ListView<>();
            list.setItems(observableList);
            list.setPrefHeight(360);
            list.setPrefWidth(580);
            Pane pane = new Pane(list);
            pane.setPrefSize(600, 400);
            Stage stage = new Stage();
            stage.setWidth(600);
            stage.setHeight(400);
            stage.setScene(new Scene(pane, 600, 400));
            stage.setTitle("Dictionary");
            stage.show();
        }
        else {
            showErrorAlert("Please press start before trying to load dictionaries, or provide a path for an existing one");
        }
    }

    /**
     * this function loads the dictionaries to the memory.
     */
    public void loadDictionaries(){
        initControllerForExistingFiles();
        if (controller != null){
            controller.uploadDictionaries();
            showInformationAlert("Loading dictionaries successfully.");
        }
        else {
            showErrorAlert("Please press start before trying to load dictionaries, or provide a path for an existing one");
        }
    }

    /**
     * this function resets the memory and delets the files that were created during indexing operation.
     */
    public void reset(){
        initControllerForExistingFiles();
        if (controller != null){
            controller.reset();
            showInformationAlert("Reset system successfully.");
        }
        else {
            showErrorAlert("Please press start before trying to load dictionaries, or provide a path for an existing one");
        }
    }

    /**
     * this is an helper function, that initiate the controller object if a path was given.
     */
    public void initControllerForExistingFiles(){
        String dicPath = this.pathForDictionaryAndPosting.getText();
        if (!dicPath.equals("")){
            if (dicPath.contains("\\") && dicPath.charAt(dicPath.length()-1) != '\\'){
                dicPath = dicPath+"\\";
            }
            if (dicPath.contains("/") && dicPath.charAt(dicPath.length()-1) != '/'){
                dicPath = dicPath+"/";
            }
            File folder = new File(dicPath);
            File [] files = folder.listFiles();
            Set<String> fileNames = new HashSet<>();
            List<String> filesList = new ArrayList<>();
            if (stemming.isSelected()){
                filesList.add("stemmedDictionaryFile");
                filesList.add("stemmedDictionaryToShow");
                filesList.add("stemmedPostingFile");
            }
            else {
                filesList.add("dictionaryFile");
                filesList.add("dictionaryFileToShow");
                filesList.add("postingFile");
            }
            filesList.add("citiesDictionaryFile");
            filesList.add("citiesPostingFile");
            for (File file: files) {
                fileNames.add(file.getName());
            }
            if (fileNames.containsAll(filesList)){
                controller = new Controller(dicPath, stemming.isSelected());
            }
        }
    }

    /**
     * generic information alert.
     */
    private void showInformationAlert(String stringAlert){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Message");
        alert.setContentText(stringAlert);
        alert.show();
    }

    /**
     * generic error alert.
     */
    private void showErrorAlert(String stringAlert){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Message");
        alert.setContentText(stringAlert);
        alert.showAndWait();
    }
}
