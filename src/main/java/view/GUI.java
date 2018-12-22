package view;
import controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import model.Entity;
import model.Result;
import org.controlsfx.control.CheckComboBox;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

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
    @FXML
    private TextField singleQuery;
    @FXML
    private TextField pathForQueryFile;
    @FXML
    private Button runQuery;
    @FXML
    private Button showCitiesList;
    @FXML
    private CheckBox semantic;
    @FXML
    private Button browse3;

    private Controller controller;
    private CheckComboBox<String> checkComboBox;
    private boolean stemStatus;


    public Controller getController() {
        return controller;
    }

    /**
     * first browse function, opens the browse directory option.
     */
    public void browseCorpusDir(){
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
    public void browsePostingDir(){
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(new Stage());
        if (file == null){
            showErrorAlert("No directory selected.");
        }
        else {
            pathForDictionaryAndPosting.setText(file.getAbsolutePath());
        }
    }

    public void browseQueryFile(){
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile == null){
            showErrorAlert("No file selected.");
        }
        else {
            pathForQueryFile.setText(selectedFile.getAbsolutePath());
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
            loadDictionaries();
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
        if (controller == null)
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
        if (controller == null || stemStatus != stemming.isSelected())
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
        if (controller == null)
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
    public void initControllerForExistingFiles() {
        if (this.pathForCorpusAndStopWords.getText().equals("") || this.pathForDictionaryAndPosting.getText().equals(""))
            showErrorAlert("You must enter the corpus path and the posting path.");
        else {
            String corpusAndStopWordsPath = this.pathForCorpusAndStopWords.getText();
            File file1 = new File(corpusAndStopWordsPath);
            String stopWordsPath;
            if (file1.isDirectory()) {
                if (corpusAndStopWordsPath.contains("\\")) {
                    corpusAndStopWordsPath = replaceChars(corpusAndStopWordsPath, "\\", "/");
                    stopWordsPath = corpusAndStopWordsPath + "/stop_words.txt";
                } else {
                    stopWordsPath = corpusAndStopWordsPath + "/stop_words.txt";
                }
                String dicPath = this.pathForDictionaryAndPosting.getText();
                if (!dicPath.equals("")) {
                    if (dicPath.contains("\\") && dicPath.charAt(dicPath.length() - 1) != '\\') {
                        dicPath = dicPath + "\\";
                    }
                    if (dicPath.contains("/") && dicPath.charAt(dicPath.length() - 1) != '/') {
                        dicPath = dicPath + "/";
                    }
                    File folder = new File(dicPath);
                    File[] files = folder.listFiles();
                    Set<String> fileNames = new HashSet<>();
                    List<String> filesList = new ArrayList<>();
                    if (stemming.isSelected()) {
                        this.stemStatus = true;
                        filesList.add("stemmedDictionaryFile");
                        filesList.add("stemmedDictionaryToShow");
                        filesList.add("stemmedPostingFile");
                    } else {
                        this.stemStatus = false;
                        filesList.add("dictionaryFile");
                        filesList.add("dictionaryFileToShow");
                        filesList.add("postingFile");
                    }
                    filesList.add("citiesDictionaryFile");
                    filesList.add("citiesPostingFile");
                    for (File file : files) {
                        fileNames.add(file.getName());
                    }
                    if (fileNames.containsAll(filesList)) {
                        controller = new Controller(stopWordsPath, dicPath, stemming.isSelected());
                    }
                }
            }
        }
    }

    public void runQuery() {
        if (stemming.isSelected() != stemStatus){
            showInformationAlert("You changed the stemming checkbox, please load the dictionaries again.");
            return;
        }
        if (controller == null)
            initControllerForExistingFiles();
        if (controller == null)
            return;
        if (controller != null && controller.getUploadDictionary().getDictionary().size() == 0){
            showErrorAlert("You must load the dictionaries first.");
        }
        if ((pathForQueryFile.getText().equals("") && singleQuery.getText().equals("")) || (!pathForQueryFile.getText().equals("") && !singleQuery.getText().equals(""))){
            showErrorAlert("Please enter a single query or a query file before run query.");
        }
        else if (!singleQuery.getText().equals("")) {
            String query = singleQuery.getText();
            List<String> chosenCities = new ArrayList<>();
            if (checkComboBox != null) {
                ObservableList<String> chosenCitiesObservable = checkComboBox.getCheckModel().getCheckedItems();
                chosenCities.addAll(chosenCitiesObservable);
            }
            Map<String, Double> queryResult = controller.runQuery(chosenCities, query, stemming.isSelected(), semantic.isSelected(), "");
            TableView<Result> tableView = new TableView<>();
            addColumnsDocs(tableView);
            ObservableList<Result> data = FXCollections.observableArrayList();

            for (Iterator<Map.Entry<String, Double>> it = queryResult.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Double> entry = it.next();
                String docNumber = entry.getKey();
                double rank = entry.getValue();
                data.add(new Result(docNumber, String.valueOf(rank)));
            }
            tableView.setItems(data);
            //FINALLY ADDED TO TableView
            SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(Orientation.VERTICAL);
            Button button1 = new Button("Save");
            button1.setOnAction(click -> {
                saveSingleQuery(tableView);
            });
            Button button2 = new Button("Show Entities for document");
            button2.setOnAction(click -> {
                showMaxEntities(tableView);
            });
            splitPane.getItems().addAll(tableView,button1, button2);
            Scene scene = new Scene(splitPane);
            Stage stage = new Stage();
            stage.setTitle("Query Result");
            stage.setScene(scene);
            stage.show();
        }
    }

    private void showMaxEntities(TableView<Result> tableView) {
        ObservableList<Result> chosenDoc = tableView.getSelectionModel().getSelectedItems();
        if (chosenDoc.size() < 1) {
            showErrorAlert("You must choose a document first.");
        }
        else if (chosenDoc.size() > 1){
            showErrorAlert("You must choose only one document at a time.");
        }
        else {
            Result result = chosenDoc.get(0);
            Map<String, Integer> entitiesMap = controller.getMaxEntities(result.getDocNumber());
            if (entitiesMap.size() == 0){
                showInformationAlert("There are no entities for this document.");
                return;
            }
            TableView<Entity> entitiesTableView = new TableView<>();
            addColumnsEntities(entitiesTableView);
            ObservableList<Entity> data = FXCollections.observableArrayList();

            for (Iterator<Map.Entry<String, Integer>> it = entitiesMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Integer> entry = it.next();
                String entityName = entry.getKey();
                int entityRank = entry.getValue();
                data.add(new Entity(entityName, String.valueOf(entityRank)));
            }
            entitiesTableView.setItems(data);
            //FINALLY ADDED TO TableView
            SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.getItems().addAll(entitiesTableView);
            Scene scene = new Scene(splitPane);
            Stage stage = new Stage();
            stage.setTitle("Max Entities for document: "+result.getDocNumber());
            stage.setScene(scene);
            stage.show();
        }
    }

    private void saveSingleQuery(TableView<Result> tableView){
        if (tableView.getItems().size() == 0) {
            showInformationAlert("There is no results to save.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.setInitialFileName("resultFile.txt");
        Stage savedStage = new Stage();
        File savedFile = fileChooser.showSaveDialog(savedStage);
        if (savedFile != null) {
            try {
                Random random = new Random();
                int queryId = random.nextInt(999);
                PrintWriter pw = new PrintWriter(savedFile);
                ObservableList<Result> resultList = tableView.getItems();
                for (int i = 0; i < resultList.size(); i++) {
                    Result result = resultList.get(i);

                    pw.println(queryId + " " + "0" + " " + result.getDocNumber() + " " + "1" + " " + result.getRank() + " " + "mt");
                }
                pw.flush();
                pw.close();
                showInformationAlert("The query result was saved successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void addColumnsDocs(TableView<Result> tableView) {
        TableColumn <Result,String> clm1 = new TableColumn("Document Number");
        clm1.setCellValueFactory(new PropertyValueFactory<>("docNumber"));
        TableColumn <Result,String> clm2 = new TableColumn("Rank");
        clm2.setCellValueFactory(new PropertyValueFactory<>("rank"));
        tableView.getColumns().addAll(clm1,clm2);
    }

    private void addColumnsEntities(TableView<Entity> tableView) {
        TableColumn <Entity,String> clm1 = new TableColumn("Entity Name");
        clm1.setCellValueFactory(new PropertyValueFactory<>("entityName"));
        TableColumn <Entity,String> clm2 = new TableColumn("Entity Rank");
        clm2.setCellValueFactory(new PropertyValueFactory<>("entityRank"));
        tableView.getColumns().addAll(clm1,clm2);
    }

    public void showCitiesList(){
        if (controller == null){
            initControllerForExistingFiles();
        }
        if (controller == null)
            return;
        if (controller != null && controller.getUploadDictionary().getCitiesDictionary().size() == 0) {
            showErrorAlert("You must load the dictionaries first.");
        }
        else {
            Map<String, List<String>> citiesDictionary = controller.getUploadDictionary().getCitiesDictionary();
            //System.out.println(citiesDictionary.size());
            Map<String, List<String>> sortedCities;
            sortedCities = citiesDictionary.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            //System.out.println(sortedCities.size());
            ObservableList<String> citiesList = FXCollections.observableArrayList();
            for (Iterator<Map.Entry<String, List<String>>> it = sortedCities.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, List<String>> entry = it.next();
                String city = entry.getKey();
                citiesList.add(city.toUpperCase());
            }
            //System.out.println(citiesList);
            //System.out.println(citiesList.size());
            checkComboBox = new CheckComboBox<>();
            checkComboBox.getItems().setAll(citiesList);
            //System.out.println(checkComboBox.getItems().size());
            gridPane.add(checkComboBox, 1,4);
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
