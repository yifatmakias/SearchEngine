package view;
import controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.Pair;
import model.Entity;
import model.Result;
import org.controlsfx.control.CheckComboBox;
import static org.apache.commons.lang3.StringUtils.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

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
    private javafx.scene.control.CheckBox stemming;
    @FXML
    private TextField singleQuery;
    @FXML
    private TextField pathForQueryFile;
    @FXML
    private CheckBox semantic;


    private Controller controller;
    private CheckComboBox<String> checkComboBox;
    private ListView<String> citiesListView;
    private ComboBox<String> languagesComboBox;
    private boolean stemStatus;


    public Controller getController() {
        return controller;
    }

    /**
     * First browse function, opens the browse directory option for corpus and stop words dir.
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
     * Second browse function, opens the browse directory option for posting files dir.
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

    /**
     * Third browse function, opens the browse file option to choose a queries file.
     */
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
     * On action for the start button.
     * Shows the results from the start button to an information alert.
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
     * This function shows the dictionary after starting the system, or loading a valid dictionary path.
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
                    if (line != null) {
                        String term = line.split(":")[0];
                        if (term.length() > 1 && !term.startsWith(",") && !term.startsWith("."))
                            observableList.add(line);
                    }
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
    }

    /**
     * This function loads the dictionaries to the memory.
     */
    public void loadDictionaries(){
        if (controller == null || stemStatus != stemming.isSelected())
            initControllerForExistingFiles();
        if (controller != null){
            try {
                controller.uploadDictionaries();
                showInformationAlert("Loading dictionaries successfully.");
            }
            catch (Exception e) {
                showErrorAlert("Loading failed, check that the files in the given directory are valid.");
            }
        }
    }

    /**א
     * This function resets the memory and delets the files that were created during indexing operation.
     */
    public void reset(){
        if (controller == null) {
            initControllerForExistingFiles();
        }
        if (controller != null){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset");
            alert.setHeaderText("");
            alert.setContentText("Are you sure you want to reset the system?\n Notice that all the files will be deleted");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                controller.reset();
                showInformationAlert("Reset system successfully.");
            }
        }
    }

    /**
     * This is an helper function, that initiate the controller object if a path was given.
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
                        filesList.add("stemmedDocsData");
                    } else {
                        this.stemStatus = false;
                        filesList.add("dictionaryFile");
                        filesList.add("dictionaryFileToShow");
                        filesList.add("postingFile");
                        filesList.add("docsData");
                    }
                    filesList.add("citiesDictionaryFile");
                    filesList.add("citiesPostingFile");
                    for (File file : files) {
                        fileNames.add(file.getName());
                    }
                    if (fileNames.containsAll(filesList)) {
                        controller = new Controller(stopWordsPath, dicPath, stemming.isSelected());
                    }
                    else {
                        showErrorAlert("The posting path is not valid, please try again.");
                    }
                }
            }
        }
    }

    /**
     * This function runs a query that was entered to the single query textField or to the queryFile textField.
     */
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
        else if (!singleQuery.getText().equals("")) { // single query
            String query = singleQuery.getText();
            String chosenLanguage = "";
            if (languagesComboBox != null) {
                chosenLanguage = languagesComboBox.getSelectionModel().getSelectedItem();
            }
            List<String> chosenCities = new ArrayList<>();

            if (citiesListView != null) {
                ObservableList<String> chosenCitiesObservable = citiesListView.getSelectionModel().getSelectedItems();
                chosenCities.addAll(chosenCitiesObservable);
            }
            /**
            if (checkComboBox != null) {
                ObservableList<String> chosenCitiesObservable = checkComboBox.getCheckModel().getCheckedItems();
                chosenCities.addAll(chosenCitiesObservable);
            }**/
            Map<String, Double> queryResult = controller.runQuery(chosenCities, query, stemming.isSelected(), semantic.isSelected(), "", chosenLanguage);
            Random random = new Random();
            int queryNum = random.nextInt(999);
            List<Pair<String, Map<String, Double>>> queryResults = new ArrayList<>();
            Pair<String, Map<String, Double>> queryPair = new Pair<>(String.valueOf(queryNum), queryResult);
            queryResults.add(queryPair);
            showQueryResult(queryResults);
        }
        else if (!pathForQueryFile.getText().equals("")) { // query file.
            File queryFile = new File(pathForQueryFile.getText());
            if (!queryFile.isFile()) {
                showErrorAlert("The file is not valid, please enter a new one.");
            }
            else {
                String chosenLanguage = "";
                if (languagesComboBox != null) {
                    chosenLanguage = languagesComboBox.getSelectionModel().getSelectedItem();
                }
                List<String> chosenCities = new ArrayList<>();
                if (checkComboBox != null) {
                    ObservableList<String> chosenCitiesObservable = checkComboBox.getCheckModel().getCheckedItems();
                    chosenCities.addAll(chosenCitiesObservable);
                }
                List<Pair<String, Map<String, Double>>> queryResults = new ArrayList<>();
                List<List<String>> parsedQueryFile = parseQueryFile(pathForQueryFile.getText());
                for (List<String> queryDetails: parsedQueryFile) {
                    String queryNum = queryDetails.get(0);
                    String query = queryDetails.get(1);
                    String desc = queryDetails.get(2);
                    Map<String, Double> queryResult = controller.runQuery(chosenCities, query, stemming.isSelected(), semantic.isSelected(), desc, chosenLanguage);
                    Pair<String, Map<String, Double>> queryPair = new Pair<>(queryNum, queryResult);
                    queryResults.add(queryPair);
                }
                showQueryResult(queryResults);
            }
        }
    }

    /**
     * Helper function - shows the query result.
     */
    private void showQueryResult(List<Pair<String, Map<String, Double>>> queryResults) {
        TableView<Result> tableView = new TableView<>();
        addColumnsDocs(tableView);
        ObservableList<Result> data = FXCollections.observableArrayList();
        for (Pair<String, Map<String, Double>> queryPair: queryResults) {
            String queryNum = queryPair.getKey();
            Map<String, Double> queryResult = queryPair.getValue();
            for (Iterator<Map.Entry<String, Double>> it = queryResult.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Double> entry = it.next();
                String docNumber = entry.getKey();
                double rank = entry.getValue();
                data.add(new Result(String.valueOf(queryNum), docNumber, String.valueOf(rank)));
            }
        }
        tableView.setItems(data);
        //FINALLY ADDED TO TableView
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        Button button1 = new Button("Save");
        button1.setOnAction(click -> {
            save(tableView);
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

    /**
     * Helper function - shows the max entities.
     */
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
            Map<String, Double> entitiesMap = controller.getMaxEntities(result.getDocNumber());
            if (entitiesMap.size() == 0){
                showInformationAlert("There are no entities for this document.");
                return;
            }
            TableView<Entity> entitiesTableView = new TableView<>();
            addColumnsEntities(entitiesTableView);
            ObservableList<Entity> data = FXCollections.observableArrayList();

            for (Iterator<Map.Entry<String, Double>> it = entitiesMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Double> entry = it.next();
                String entityName = entry.getKey();
                double entityRank = entry.getValue();
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

    /**
     * Helper function - saves the table view result to a file.
     */
    private void save(TableView<Result> tableView){
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
                PrintWriter pw = new PrintWriter(savedFile);
                ObservableList<Result> resultList = tableView.getItems();
                for (int i = 0; i < resultList.size(); i++) {
                    Result result = resultList.get(i);
                    pw.println(result.getQueryNum() + " " + "0" + " " + result.getDocNumber() + " " + "1" + " " + result.getRank() + " " + "mt");
                }
                pw.flush();
                pw.close();
                showInformationAlert("The query/queries result was saved successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Helper function - adds columns to Result table view.
     */
    private void addColumnsDocs(TableView<Result> tableView) {
        TableColumn <Result,String> clm1 = new TableColumn("Query Number");
        clm1.setCellValueFactory(new PropertyValueFactory<>("queryNum"));
        TableColumn <Result,String> clm2 = new TableColumn("Document Number");
        clm2.setCellValueFactory(new PropertyValueFactory<>("docNumber"));
        TableColumn <Result,String> clm3 = new TableColumn("Rank");
        clm3.setCellValueFactory(new PropertyValueFactory<>("rank"));
        tableView.getColumns().addAll(clm1,clm2, clm3);
    }

    /**
     * Helper function - adds columns to Entity table view.
     */
    private void addColumnsEntities(TableView<Entity> tableView) {
        TableColumn <Entity,String> clm1 = new TableColumn("Entity Name");
        clm1.setCellValueFactory(new PropertyValueFactory<>("entityName"));
        TableColumn <Entity,String> clm2 = new TableColumn("Entity Rank");
        clm2.setCellValueFactory(new PropertyValueFactory<>("entityRank"));
        tableView.getColumns().addAll(clm1,clm2);
    }

    /**
     * This function shows the cities list.
     */
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
            ObservableList<String> citiesList = FXCollections.observableArrayList();
            for (Iterator<Map.Entry<String, List<String>>> it = citiesDictionary.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, List<String>> entry = it.next();
                String city = entry.getKey();
                if (!city.matches(".*\\d+.*"))
                    citiesList.add(city.toUpperCase());
            }
            Collections.sort(citiesList);

            citiesListView = new ListView<>(citiesList);
            citiesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            gridPane.add(citiesListView, 1,4);
            /**
            checkComboBox = new CheckComboBox<>();
            checkComboBox.getItems().setAll(citiesList);
            gridPane.add(checkComboBox, 1,4);**/
        }
    }

    /**
     * This function shows the languages list.
     */
    public void showLanguagesList(){
        if (controller == null){
            initControllerForExistingFiles();
        }
        if (controller == null)
            return;
        else {
            try {
                BufferedReader br;
                if (stemming.isSelected())
                    br = new BufferedReader(new FileReader(controller.getDicPath() + "stemmedDocsData"));
                else
                    br = new BufferedReader(new FileReader(controller.getDicPath() + "docsData"));
                String line = br.readLine();
                while (line != null) {
                    String[] splitedLine = line.split("\\$");
                    String docNumber = splitedLine[0];
                    List<String> docList = new ArrayList<>();
                    for (int i = 1; i < splitedLine.length; i++) {
                        docList.add(splitedLine[i]);
                    }
                    controller.docsDataNoStart.put(docNumber, docList);
                    line = br.readLine();
                }
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            Set<String> languages = new HashSet<>();
            for (Iterator<Map.Entry<String, List<String>>> it = controller.docsDataNoStart.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, List<String>> entry = it.next();
                String language = entry.getValue().get(2);
                if (!language.equals("noLanguage") && !language.matches(".*\\d+.*")) {
                    languages.add(language);
                }
            }
            List<String> languagesArrList = new ArrayList<>(languages);
            Collections.sort(languagesArrList);
            ObservableList<String> languagesList = FXCollections.observableArrayList();
            languagesList.setAll(languagesArrList);
            languagesComboBox = new ComboBox<>();
            languagesComboBox.getItems().setAll(languagesList);
            gridPane.add(languagesComboBox, 1,5);
        }
    }

    /**
     * This function parse the query file and returns a map that represent it.
     */
    public List<List<String>> parseQueryFile(String queriesPath) {
        List<List<String>> parsedQueryFile = new ArrayList<>();
        try {
            File f = new File(queriesPath);
            BufferedReader br = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }

            String [] tops = sb.toString().split("</top>");
            for (String top: tops) {
                String [] splitedTop = top.split("<\\S+>");
                String num = splitedTop[1].split("Number: ")[1].trim();
                String title = splitedTop[2].trim();
                String desc = splitedTop[3].split("Description: ")[1].trim();
                List<String> queryDetail = new ArrayList<>();
                queryDetail.add(num);
                queryDetail.add(title);
                queryDetail.add(desc);
                parsedQueryFile.add(queryDetail);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return parsedQueryFile;
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
