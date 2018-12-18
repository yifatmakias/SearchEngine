package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Searcher;
import model.UploadDictionary;

import java.util.ArrayList;
import java.util.List;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/GUI.fxml"));
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 800, 550));
        primaryStage.show();
    }

    public static void main(String[] args) {
        List<String> cities = new ArrayList<>();
        UploadDictionary uploadDictionary = new UploadDictionary("C:/Users/yifat/postingDir/citiesDictionaryFile", "C:/Users/yifat/postingDir/dictionaryFile");
        Searcher searcher = new Searcher("British Chunnel impact", uploadDictionary, "C:/Users/yifat/postingDir/postingFile", false, "C:/Users/yifat/postingDir/docsData", cities, "C:/Users/yifat/postingDir/citiesPostingFile");
        searcher.queryHandle();
        System.out.println(searcher.getResultForQuery());
        launch(args);
    }
}
