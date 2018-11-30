package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.*;
import org.json.*;

import java.io.*;
import java.util.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static List<String> updateCityDetails(String cityName){
        List<String> details = new ArrayList<>();

        return details;
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
        Map<String, Boolean> upperLowerDic = new HashMap<>();
        Map<String, List<String>> cityMap = new HashMap<>();
        Parse parse = new Parse(stopWords, upperLowerDic);
        Boolean toStem = false;
        int fileCounter = 1;
        int fileCounterIndex = 0;
        int docNum = 0;
        double doubleChunkSize = 40.0;
        int intChunkSize = 40;
        double indexSize = files.length/doubleChunkSize;
        String indexSizeString = String.valueOf(indexSize);
        String [] splitedDobule = indexSizeString.split("\\.");
        List<Thread> indexerThredsList = new ArrayList<>();
        boolean isRest = false;
        boolean lastIndexer = false;
        if (Integer.valueOf(splitedDobule[1]) > 0){
            isRest = true;
        }

        for (File file:files)
        {
            if (file.isDirectory())
            {
                ReadFile readFile = new ReadFile(file.getAbsolutePath()+"\\"+file.getName());
                readFile.fillDocumentSet();
                for (Doc doc: readFile.getDocumentSet()) {
                    String cityName = doc.getCity();
                    docNum++;
                    parse.parse(doc,null , toStem);
                    doc.setText("");
                }
                if (fileCounterIndex == Integer.valueOf(splitedDobule[0]) && isRest){
                    lastIndexer = true;
                }
                if (fileCounter == intChunkSize || lastIndexer && fileCounter == files.length % intChunkSize){
                    fileCounterIndex++;
                    parse.removeStopWords();
                    Indexer indexer = new Indexer("C:/Users/yifat/postingDir/", toStem, fileCounterIndex, parse.getTerms());
                    Thread thread = new Thread(indexer);
                    thread.start();
                    indexerThredsList.add(thread);
                    parse = new Parse(stopWords, upperLowerDic);
                    fileCounter = 0;
                }
                fileCounter++;
            }
        }
        //System.out.println(docNum);
        for (Thread t: indexerThredsList) {
            try {
                t.join();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Merge merge = new Merge("C:/Users/yifat/postingDir/", upperLowerDic, toStem);
        merge.merge();
        merge.separatePosting();
        /**
        for (Iterator<Map.Entry<String, Boolean>> it = upperLowerDic.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Boolean> entry = it.next();
            Boolean bool = entry.getValue();
            if (bool == false){
                System.out.println(entry.getKey());
            }
        }
        try {
            FileReader file = new FileReader("C:/Users/yifat/postingDir/mergedPosting");
            BufferedReader br = new BufferedReader(file);
            int lineNumber = 0;
            String line;
            while ((line = br.readLine()) != null){
                System.out.println(line);
                lineNumber++;
            }
            System.out.println(lineNumber);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            RandomAccessFile file = new RandomAccessFile("C:/Users/yifat/postingDirTemp/file7", "r");
            file.seek(0);
            String firstLine = file.readLine();
            System.out.println(firstLine);
            file.seek(firstLine.getBytes().length+2);
            System.out.println(file.readLine());
        }
        catch (Exception e){
            e.printStackTrace();
        }**/
        //System.out.println("________________________________________");
        //System.out.println(parse.getTerms());

        durationMS += System.currentTimeMillis() - start;
        System.out.println(durationMS);
        launch(args);
    }
}
