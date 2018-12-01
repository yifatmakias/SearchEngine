package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadDictionary {

    private String citiesDictionaryPath;
    private String dictionaryPath;
    private Map<String, List<String>> citiesDictionary;
    private Map<String, List<Integer>> dictionary;

    public UploadDictionary(String citiesDictionaryPath, String dictionaryPath) {
        this.citiesDictionaryPath = citiesDictionaryPath;
        this.dictionaryPath = dictionaryPath;
        this.citiesDictionary = new HashMap<>();
        this.dictionary = new HashMap<>();
    }

    public Map<String, List<String>> getCitiesDictionary() {
        return citiesDictionary;
    }

    public Map<String, List<Integer>> getDictionary() {
        return dictionary;
    }

    public void uploadCitiesDictionary(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.citiesDictionaryPath));
            String line = br.readLine();
            while (line != null){
                String [] splitedLine = line.split(";");
                String cityKey = splitedLine[0];
                List<String> cityDetails = new ArrayList<>();
                for (int i = 1; i < splitedLine.length ; i++) {
                    cityDetails.add(splitedLine[i]);
                }
                this.citiesDictionary.put(cityKey, cityDetails);
                line = br.readLine();
            }
            br.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void uploadDictionary(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.dictionaryPath));
            String line = br.readLine();
            while (line != null){
                String [] splitedLine = line.split(";");
                String termKey = splitedLine[0];
                List<Integer> termDetails = new ArrayList<>();
                for (int i = 1; i < splitedLine.length ; i++) {
                    termDetails.add(Integer.valueOf(splitedLine[i]));
                }
                this.dictionary.put(termKey, termDetails);
                line = br.readLine();
            }
            br.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
