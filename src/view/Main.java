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
import java.io.File;
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
        long start;
        start = System.currentTimeMillis();

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

        File folder = new File("C:/Users/yifat/corpus/");
        File[] files = folder.listFiles();
        Parse parse = new Parse(stopWords);

        for (File file:files)
        {
            if (file.isDirectory())
            {
                ReadFile readFile = new ReadFile(file.getAbsolutePath()+"\\"+file.getName());
                readFile.fillDocumentSet();
                for (Doc doc: readFile.getDocumentSet()) {
                    //System.out.println(doc.getDocNumber());
                    parse.parse(doc.getText());
                }
            }
        }
        parse.removeStopWords();
        //System.out.println(parse.getTerms());
        durationMS += System.currentTimeMillis() - start;
        System.out.println(parse.getTerms().size());
        System.out.println(durationMS);


        launch(args);
    }
}
