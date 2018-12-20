package model;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Searcher {
    private String query;
    private Map<String, Double> resultForQuery;
    private Parse parse;
    private UploadDictionary uploadDictionary;
    private String postingPath;
    private boolean toStem;
    private Map<String, List<String>> docsMap;
    private List<String> chosenCities;
    private String citiesPostingPath;
    private Ranker ranker;

    public Searcher(String query, UploadDictionary uploadDictionary, String postingPath, boolean toStem, List<String> chosenCities, String citiesPostingPath, Map<String, List<String>> docsMap) {
        this.query = query;
        this.parse = new Parse();
        this.uploadDictionary = uploadDictionary;
        this.postingPath = postingPath;
        this.toStem = toStem;
        this.chosenCities = chosenCities;
        this.citiesPostingPath = citiesPostingPath;
        this.docsMap = new HashMap<>();
        this.docsMap = docsMap;
        //setDocsMap();
    }

    /**
     public void setDocsMap() {
     try {
     FileInputStream fileInputStream = new FileInputStream(this.docsMapPath);
     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
     this.docsMap = (Map) objectInputStream.readObject();
     fileInputStream.close();
     objectInputStream.close();
     }
     catch (Exception e){
     e.printStackTrace();
     }
     }**/

    public Map<String, Double> getResultForQuery() {
        return resultForQuery;
    }

    public void queryHandle() {
        String [] querySplit = query.split(" ");
        List<String> queryList = new ArrayList<>();
        for (int i = 0; i <querySplit.length ; i++) {
            parse.parse(null, querySplit[i], this.toStem);
            Map<String, Term> queryTerms = parse.getTerms();
            for (Iterator<Map.Entry<String, Term>> it = queryTerms.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Term> entry = it.next();
                String queryTerm = entry.getKey();
                queryList.add(queryTerm);
            }
        }
        double docsCount_M = this.docsMap.size();
        long sumDocsLength = 0;
        for (Iterator<Map.Entry<String, List<String>>> it = docsMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> docData= entry.getValue();
            int docLength = Integer.valueOf(docData.get(0));
            sumDocsLength += docLength;
        }
        double avdl = sumDocsLength/ docsCount_M;
        List<Pair<String, Integer>> queryToRanker = new ArrayList<>();
        List<String> postingLinesToRanker = new ArrayList<>();
        uploadDictionary.uploadDictionary();
        uploadDictionary.uploadCitiesDictionary();
        Map<String, List<Integer>> dictionary = uploadDictionary.getDictionary();
        Map<String, List<String>> citiesDictionary = uploadDictionary.getCitiesDictionary();
        for (String queryString: queryList) {
            int df = dictionary.get(queryString).get(0);
            int pointer = dictionary.get(queryString).get(2);
            Pair<String, Integer> termAndDf = new Pair<>(queryString, df);
            queryToRanker.add(termAndDf);
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(postingPath, "r");
                randomAccessFile.seek(pointer);
                String line = randomAccessFile.readLine();
                postingLinesToRanker.add(line);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Set<String> docsForQuery = getRelevantDocsForQuery(postingLinesToRanker);
        List<String> postingLinesCities = new ArrayList<>();
        for(String city: chosenCities) {
            int cityPointer = Integer.valueOf(citiesDictionary.get(city).get(3));
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(citiesPostingPath, "r");
                randomAccessFile.seek(cityPointer);
                String line = randomAccessFile.readLine();
                postingLinesCities.add(line);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Set<String> docsByCities = getRelevantDocsForQuery(postingLinesCities);
        Map<String,Integer> relevantDocsForRenker = new HashMap<>();
        docsForQuery.retainAll(docsByCities);

        for (String docNumber: docsForQuery) {
            int docLength= Integer.valueOf(docsMap.get(docNumber).get(1));
            relevantDocsForRenker.put(docNumber,docLength);
        }
        ranker = new Ranker(queryToRanker, relevantDocsForRenker, docsCount_M, avdl, postingLinesToRanker);
        ranker.rankDocs();
        this.resultForQuery = ranker.getRankedDocs();
    }

    public Set<String> getRelevantDocsForQuery(List<String> postingLines) {
        Set<String> docsForQuery = new HashSet<>();
        for (String postingLine : postingLines) {
            String[] splitLine = postingLine.split("\\$");
            for (int i = 0; i < splitLine.length - 1; i += 2) {
                String docNumber = splitLine[i];
                docsForQuery.add(docNumber);
            }
        }
        return docsForQuery;
    }
}
