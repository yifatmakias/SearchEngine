package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.*;
import org.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static List<String> updateCityDetails(String cityName) {
        List<String> details = new ArrayList<>();
        try {
            URL url = new URL("https://restcountries.eu/rest/v2/capital/" + cityName.toLowerCase());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            String jsonString = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                jsonString += line;
            }
            br.close();
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject obj = (JSONObject) jsonArray.get(0);
            details.add((String)obj.get("name"));
            String population =String.valueOf(obj.get("population"));
            details.add(convertPopulotianNum(population));
            JSONArray currency = obj.getJSONArray("currencies");
            JSONObject currencyObj = (JSONObject) currency.get(0);
            details.add((String)currencyObj.get("code"));
        } catch (Exception e) {
            if (e instanceof  FileNotFoundException){
                try {
                    URL url = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + cityName.toLowerCase());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    String geoString = "";
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        geoString += line;
                    }
                    br.close();
                    JSONObject obj = new JSONObject(geoString);
                    details.add((String)obj.get("geobytescountry"));
                    String population =String.valueOf(obj.get("geobytespopulation"));
                    details.add(convertPopulotianNum(population));
                    details.add((String)obj.get("geobytescurrencycode"));
                }
                catch (Exception e1){
                    //e1.printStackTrace();
                }
            }
            //e.printStackTrace();
        }
        return details;
    }

    public static String convertPopulotianNum(String popNum){
        Parse parse = new Parse();
        Term term = null;
        parse.parse(null, popNum, false);
        for (Iterator<Map.Entry<String, Term>> it = parse.getTerms().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Term> entry = it.next();
            term = entry.getValue();
        }
        return term.getTerm();
    }

    public static void main(String[] args) {
        long durationMS = 0;
        long start;
        start = System.currentTimeMillis();
        Set<String> stopWords = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:/Users/yifat/corpus/stop_words.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!(line.equals("b") || line.equals("c") || line.equals("d") || line.equals("e") || line.equals("f") || line.equals("g") || line.equals("h") || line.equals("i") || line.equals("j") || line.equals("k") || line.equals("l") || line.equals("m") || line.equals("n") || line.equals("o") || line.equals("p") || line.equals("q") || line.equals("r") || line.equals("s") || line.equals("t") || line.equals("u") || line.equals("v") || line.equals("w") || line.equals("x") || line.equals("y") || line.equals("z")))
                    stopWords.add(line);
            }
        } catch (IOException e) {
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
        double indexSize = files.length / doubleChunkSize;
        String indexSizeString = String.valueOf(indexSize);
        String[] splitedDobule = indexSizeString.split("\\.");
        List<Thread> indexerThredsList = new ArrayList<>();
        Map<String, Doc> docsData = new HashMap<>();
        boolean isRest = false;
        boolean lastIndexer = false;
        if (Integer.valueOf(splitedDobule[1]) > 0) {
            isRest = true;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                ReadFile readFile = new ReadFile(file.getAbsolutePath() + "\\" + file.getName());
                readFile.fillDocumentSet();
                for (Doc doc : readFile.getDocumentSet()) {
                    String cityName = doc.getCity();
                    if (!cityName.equals("") && !cityMap.containsKey(cityName)){
                        List<String> cityData = updateCityDetails(cityName);
                        cityMap.put(cityName, cityData);
                    }
                    docNum++;
                    parse.parse(doc, null, toStem);
                    doc.setText("");
                    docsData.put(doc.getDocNumber(), doc);
                }
                if (fileCounterIndex == Integer.valueOf(splitedDobule[0]) && isRest) {
                    lastIndexer = true;
                }
                if (fileCounter == intChunkSize || lastIndexer && fileCounter == files.length % intChunkSize) {
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
        for (Thread t : indexerThredsList) {
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Merge merge = new Merge("C:/Users/yifat/postingDir/", upperLowerDic, toStem, cityMap);
        merge.merge();
        merge.separatePosting();
        merge.separateCitiesPosting();

        /**
        for (Iterator<Map.Entry<String, Boolean>> it = upperLowerDic.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Boolean> entry = it.next();
            Boolean bool = entry.getValue();
            if (bool == false) {
                System.out.println(entry.getKey());
            }
        }
        try {
            FileReader file = new FileReader("C:/Users/yifat/postingDir/mergedPosting");
            BufferedReader br = new BufferedReader(file);
            int lineNumber = 0;
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                lineNumber++;
            }
            System.out.println(lineNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            RandomAccessFile file = new RandomAccessFile("C:/Users/yifat/postingDirTemp/file7", "r");
            file.seek(0);
            String firstLine = file.readLine();
            System.out.println(firstLine);
            file.seek(firstLine.getBytes().length + 2);
            System.out.println(file.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("________________________________________");
        //System.out.println(parse.getTerms());

        try {
            PrintWriter pw = new PrintWriter("C:/Users/yifat/postingDir/mapDic");
            for (File file : files) {
                if (file.isDirectory()) {
                    ReadFile readFile = new ReadFile(file.getAbsolutePath() + "\\" + file.getName());
                    readFile.fillDocumentSet();
                    for (Doc doc : readFile.getDocumentSet()) {
                        if (!doc.getCity().equals(""))
                            pw.println(doc.getCity()+";"+doc.getDocNumber()+";"+doc.getFile_path());
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }**/

        durationMS += System.currentTimeMillis() - start;
        System.out.println(durationMS);
        launch(args);
    }
}
