package view;

import controller.Controller;
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

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/GUI.fxml"));
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 800, 550));
        primaryStage.show();
    }

    public static void main(String[] args) {
        //parseQueryFile();
        Controller controller = new Controller("C:/Users/yifat/corpus/stop_words.txt","C:/Users/yifat/postingDir/",false);
        controller.uploadDictionaries();
        List<String> cities = new ArrayList<>();
        //cities.add("paris");

        long durationMS = 0;
        long start;
        start = System.currentTimeMillis();
        controller.runQuery(cities, "British Chunnel impact", false, false);
        durationMS += System.currentTimeMillis() - start;
        Long runningTime = durationMS / 1000;
        System.out.println(runningTime);
        /**
        long durationMS1 = 0;
        long start1;
        start1 = System.currentTimeMillis();
        controller.runQuery(cities, "British Chunnel impact", false, true);
        durationMS1 += System.currentTimeMillis() - start1;
        Long runningTime1 = durationMS1 / 1000;
        System.out.println(runningTime1);**/

        //System.out.println(controller.getMaxEntities("FT922-1149"));
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

    public static void parseQueryFile() {
        try {
            File f = new File("C:/Users/yifat/Desktop/quries.txt");
            Document document = Jsoup.parse(new String(Files.readAllBytes(f.toPath())));
            Elements elements = document.getElementsByTag("top");
            for (Element element: elements) {
                System.out.println(element.getElementsByTag("num").text());
                System.out.println(element.getElementsByTag("title").text());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
