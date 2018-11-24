package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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
        Boolean toStem = false;
        int fileCounter = 0;
        int fileCounterIndex = 0;

        for (File file:files)
        {
            if (file.isDirectory())
            {
                fileCounter++;
                if (fileCounter == 10){
                    fileCounterIndex++;
                    parse.removeStopWords();
                    //System.out.println(parse.getTerms().size());
                    Indexer indexer = new Indexer("C:/Users/yifat/postingDir/", toStem, fileCounterIndex, parse.getTerms());
                    Thread thread = new Thread(indexer);
                    thread.start();
                    parse = new Parse(stopWords);
                    fileCounter = 0;
                }
                ReadFile readFile = new ReadFile(file.getAbsolutePath()+"\\"+file.getName());
                readFile.fillDocumentSet();
                for (Doc doc: readFile.getDocumentSet()) {
                    //System.out.println(doc.getDocNumber());
                    parse.parse(doc,null , toStem);
                    doc.setText("");
                    /**
                    if (doc.getDocNumber().equals("FBIS3-1")){
                        for (Iterator<Map.Entry<String, Term>> it = parse.getTerms().entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry<String, Term> entry = it.next();
                            Term term = entry.getValue();
                            if (term.getDocuments().containsKey("FBIS3-1")){
                                System.out.println("term:__________________________________________");
                                System.out.println(term.getTerm());
                                System.out.println("maxtf:__________________________________________");
                                System.out.println(doc.getMax_tf());
                                System.out.println("uniqe words count:__________________________________________");
                                System.out.println(doc.getUniqueWordCount());
                                System.out.println("tf, isInTitle, position, :__________________________________________");
                                System.out.println(term.getDocuments().get("FBIS3-1"));
                            }
                        }
                    }**/
                }
            }
        }
        //System.out.println("________________________________________");
        //System.out.println(parse.getTerms());
        durationMS += System.currentTimeMillis() - start;
        System.out.println(durationMS);
        launch(args);
    }
}
