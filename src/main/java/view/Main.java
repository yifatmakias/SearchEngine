package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Parse;
import model.UploadDictionary;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/GUI.fxml"));
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 800, 550));
        primaryStage.show();
    }

    public static void main(String[] args) {
        getDataForReport();
        launch(args);
    }
    public static void getDataForReport() {
        // get count of terms numbers (3)
        UploadDictionary uploadDictionary = new UploadDictionary("C:/Users/yifat/postingDir/citiesDictionaryFile", "C:/Users/yifat/postingDir/dictionaryFile");
        uploadDictionary.uploadDictionary();
        Map<String, List<Integer>> dic = uploadDictionary.getDictionary();
        int countNumbers = 0;
        Parse parse = new Parse();
        for (Iterator<Map.Entry<String, List<Integer>>> it = dic.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, List<Integer>> entry = it.next();
            String term = entry.getKey();
            term = term.replaceAll("M", "");
            term = term.replaceAll("K", "");
            term = term.replaceAll("B", "");
            if (parse.isNumeric(term) || parse.isFraction(term)){
                countNumbers++;
            }
        }
        System.out.println("count of terms numbers: "+countNumbers);

        /**
         // the city that was most time in one document (6)
         uploadDictionary.uploadCitiesDictionary();
         Map<String, List<String>> citiesDic = uploadDictionary.getCitiesDictionary();
         try {
         RandomAccessFile file = new RandomAccessFile("C:/Users/yifat/postingDir/citiesPostingFile", "r");
         Map<String, Integer> docAndTf = new HashMap<>();
         for (Iterator<Map.Entry<String, List<String>>> it = citiesDic.entrySet().iterator(); it.hasNext(); ) {
         Map.Entry<String, List<String>> entry = it.next();
         String cityName = entry.getKey();
         if (cityName.equals("moscow")){
         List<String> cityData = entry.getValue();
         int seekLine = Integer.valueOf(cityData.get(cityData.size()-1));
         file.seek(seekLine);
         String line = file.readLine();
         String [] splitedLine = line.split("\\$");
         for (int i = 0; i <splitedLine.length-1 ; i+=2) {
         String docNumber = splitedLine[i];
         int tf = Integer.valueOf(splitedLine[i+1].split(",")[0]);
         docAndTf.put(docNumber, tf);
         }
         Map<String, Integer> sortedMap = docAndTf.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
         System.out.println(sortedMap.toString());
         System.out.println(line);
         }
         }
         }
         catch (Exception e){
         e.printStackTrace();
         }**/

        // 10 most frequent terms and 10 least frequent terms
        try {
            PrintWriter pw = new PrintWriter("C:/Users/yifat/postingDir/sortedTermMap");
            Map<String, Integer> termAndTf = new HashMap<>();
            for (Iterator<Map.Entry<String, List<Integer>>> it = dic.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, List<Integer>> entry = it.next();
                String term = entry.getKey();
                List<Integer> termData = entry.getValue();
                termAndTf.put(term, termData.get(1));
            }
            Map<String, Integer> sortedMap = termAndTf.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            //int ones = 400000;
            int rank = 1;
            for (Iterator<Map.Entry<String, Integer>> it = sortedMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String,Integer> entry = it.next();
                String term = entry.getKey();
                int tf = entry.getValue();
                /**
                if (tf == 1 && ones > 0){
                    pw.println(tf);
                    ones--;
                }**/
                if (tf != 1){
                    pw.println(Math.log10(rank) + "," + Math.log10(tf));
                    rank++;
                }
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        uploadDictionary.uploadCitiesDictionary();
        Map<String, List<String>> citiesDic = uploadDictionary.getCitiesDictionary();
        Set<String> countries = new HashSet<>();
        int numOcCountries = 0;
        for (Iterator<Map.Entry<String, List<String>>> it = citiesDic.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> cityData = entry.getValue();
            if (cityData.size() == 4){
                String country = cityData.get(0);
                countries.add(country);
            }
        }
        System.out.println(countries.size());
    }
}
