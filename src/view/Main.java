package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Document;
import model.Parse;
import model.ReadFile;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        ReadFile readFile = new ReadFile("C:/Users/yifat/corpus/FB396001/FB396001");
        long durationMS = 0;
        long start = 0;
        start = System.currentTimeMillis();
        readFile.fillDocumentSet();
        durationMS += System.currentTimeMillis() - start;
        System.out.println(durationMS);
        for (Document doc: readFile.getDocumentSet()) {
            Parse parse = new Parse(doc.getText());
            System.out.println(parse);
        }
        launch(args);
    }
}
