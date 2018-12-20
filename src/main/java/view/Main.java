package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Searcher;
import model.UploadDictionary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/GUI.fxml"));
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 800, 550));
        primaryStage.show();
    }

    public static void main(String[] args) {
        runQuery();
        launch(args);
    }

    public void SetStageCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                alert.setHeaderText("");
                alert.setContentText("Are you sure you want to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    // ... user chose OK
                    // Close program
                    Platform.exit();
                    System.exit(0);
                    // } else {
                    // ... user chose CANCEL or closed the dialog
                    windowEvent.consume();
                }
            }
        });
    }

    public static void runQuery() {
        List<String> cities = new ArrayList<>();
        Map<String, List<String>> docsMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:/Users/yifat/postingDir/docsData"));
            String line = br.readLine();
            while (line != null){
                String [] splitedLine = line.split("\\$");
                String docNumber = splitedLine[0];
                List<String> docList = new ArrayList<>();
                for (int i = 1; i < splitedLine.length  ; i++) {
                    docList.add(splitedLine[i]);
                }
                docsMap.put(docNumber, docList);
                line = br.readLine();
            }
            br.close();
            UploadDictionary uploadDictionary = new UploadDictionary("C:/Users/yifat/postingDir/citiesDictionaryFile", "C:/Users/yifat/postingDir/dictionaryFile");
            Searcher searcher = new Searcher("British Chunnel impact", uploadDictionary, "C:/Users/yifat/postingDir/postingFile", false,  cities, "C:/Users/yifat/postingDir/citiesPostingFile", docsMap);
            searcher.queryHandle();
            System.out.println(searcher.getResultForQuery());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
