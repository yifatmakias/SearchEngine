package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Doc;
import model.Parse;
import model.ReadFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        long durationMS = 0;
        long start = 0;
        start = System.currentTimeMillis();

        ReadFile readFile = new ReadFile("C:/Users/yifat/corpus/FB396001/FB396001");
        Set<String> stopWords = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:/Users/yifat/corpus/stop_words.txt"));
            String line;
            while ((line = br.readLine()) != null){
                if (!(line.equals("b") || line.equals("c") || line.equals("d") || line.equals("e") || line.equals("f") || line.equals("g") ||line.equals("h") ||line.equals("i") ||line.equals("j") ||line.equals("k") ||line.equals("l") ||line.equals("m") ||line.equals("n") ||line.equals("o") ||line.equals("p") ||line.equals("q") ||line.equals("r") ||line.equals("s") ||line.equals("t") ||line.equals("u") ||line.equals("v") ||line.equals("w") ||line.equals("x") ||line.equals("y") ||line.equals("z")))
                    stopWords.add(line);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        readFile.fillDocumentSet();
        /**
        Doc doc = readFile.getDocumentSet().iterator().next();
        System.out.println(doc);
        Parse parse = new Parse(doc.getText(), stopWords);
        parse.parse();
        System.out.println(parse.getTerms());
        for (String stopWord: stopWords) {
            if (parse.getTerms().contains(new Term(stopWord))){
                System.out.println("not good");
            }
        }**/

        for (Doc doc: readFile.getDocumentSet()) {
            System.out.println(doc.getDocNumber());
            Parse parse = new Parse(doc.getText(), stopWords);
            parse.parse();
            System.out.println(parse.getTerms());
        }
        durationMS += System.currentTimeMillis() - start;
        System.out.println(durationMS);


        launch(args);
    }
}
