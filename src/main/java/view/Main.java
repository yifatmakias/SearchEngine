package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Doc;
import model.Searcher;
import model.UploadDictionary;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        Map<String, Doc> docsMap;
        try {
            FileInputStream fileInputStream = new FileInputStream("C:/Users/yifat/postingDir/docsData");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            docsMap = (HashMap<String, Doc>) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
            UploadDictionary uploadDictionary = new UploadDictionary("C:/Users/yifat/postingDir/citiesDictionaryFile", "C:/Users/yifat/postingDir/dictionaryFile");
            Searcher searcher = new Searcher("British Chunnel impact", uploadDictionary, "C:/Users/yifat/postingDir/postingFile", false,  cities, "C:/Users/yifat/postingDir/citiesPostingFile", docsMap);
            searcher.queryHandle();
            System.out.println(searcher.getResultForQuery());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        launch(args);
    }
}
