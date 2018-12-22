package model;

import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Searcher {
    private String query;
    private Map<String, Double> resultForQuery;
    private Parse parse;
    private String postingPath;
    private boolean toStem;
    private Map<String, List<String>> docsMap;
    private List<String> chosenCities;
    private String citiesPostingPath;
    private Map<String, List<Integer>> dictionary;
    private Map<String, List<String>> citiesDictionary;
    private boolean doSemantic;
    private Set<String> stopWords;
    private String description;


    public Searcher(String query, Map<String, List<Integer>> dictionary, Map<String, List<String>> citiesDictionary, String postingPath, boolean toStem, List<String> chosenCities, String citiesPostingPath, Map<String, List<String>> docsMap, boolean doSemantic, Set<String> stopWords, String description) {
        this.query = query;
        this.parse = new Parse(stopWords);
        this.postingPath = postingPath;
        this.toStem = toStem;
        this.chosenCities = chosenCities;
        this.citiesPostingPath = citiesPostingPath;
        this.docsMap = new HashMap<>();
        this.docsMap = docsMap;
        this.dictionary = dictionary;
        this.citiesDictionary = citiesDictionary;
        this.doSemantic = doSemantic;
        this.stopWords = stopWords;
        this.description = description;
    }

    public Map<String, Double> getResultForQuery() {
        return resultForQuery;
    }

    public void queryHandle() {
        Ranker ranker;
        String [] querySplit = query.split(" ");
        List<String> queryList = new ArrayList<>();
        for (int i = 0; i <querySplit.length ; i++) {
            parse.parse(null, querySplit[i], this.toStem);
            parse.removeStopWords();
            Map<String, Term> queryTerms = parse.getTerms();
            for (Iterator<Map.Entry<String, Term>> it = queryTerms.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Term> entry = it.next();
                String queryTerm = entry.getKey();
                queryList.add(queryTerm);
                if (queryTerm.contains("-")){
                    String [] splitedQueryTerm = queryTerm.split("-");
                    for (String term: splitedQueryTerm) {
                        queryList.add(term);
                    }
                }
            }
            parse = new Parse(stopWords);
        }
        if (description != null || !description.equals("")){
            parse.parse(null, description, toStem);
            parse.removeStopWords();
            Map<String, Term> descriptionTerms = parse.getTerms();
            for (Iterator<Map.Entry<String, Term>> it = descriptionTerms.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Term> entry = it.next();
                String descriptionTerm = entry.getKey();
                if (!queryList.contains(descriptionTerm))
                    queryList.add(descriptionTerm);
            }
        }
        Map<String, List<Pair<String,String>>> querySynonyms = null;
        if (doSemantic) {
            querySynonyms = getSynonyms(queryList);
        }
        double docsCount_M = this.docsMap.size();
        long sumDocsLength = 0;
        Set<String> allDocsByCities = new HashSet<>();
        for (Iterator<Map.Entry<String, List<String>>> it = docsMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> docData= entry.getValue();
            int docLength = Integer.valueOf(docData.get(0));
            sumDocsLength += docLength;
            String cityName = docData.get(1);
            if (!cityName.equals("noCity") && chosenCities.contains(cityName))
                allDocsByCities.add(entry.getKey());
        }
        double avdl = sumDocsLength/ docsCount_M;
        List<Pair<String, Integer>> queryToRanker = new ArrayList<>();
        List<Pair<String, Integer>> synonymsDataToRanker = new ArrayList<>();
        List<String> postingLinesToRanker = new ArrayList<>();
        addRelevantPostingLines(postingLinesToRanker, queryList, queryToRanker);
        List<String> synonymsPostingLines = new ArrayList<>();
        List<String> synonymsTerms = new ArrayList<>();
        if (querySynonyms != null) {
            for (Iterator<Map.Entry<String, List<Pair<String,String>>>> it = querySynonyms.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, List<Pair<String, String>>> entry = it.next();
                getSynonymsData(entry.getValue(), synonymsDataToRanker);
                for (Pair<String, String> pair: entry.getValue()) {
                    synonymsTerms.add(pair.getKey());
                    synonymsPostingLines.add(pair.getValue());
                }
            }
        }
        Set<String> relevantDocsFromSynonyms = getRelevantDocsForQuery(synonymsPostingLines);
        Set<String> docsForQuery = getRelevantDocsForQuery(postingLinesToRanker);
        docsForQuery.addAll(relevantDocsFromSynonyms);
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
        allDocsByCities.addAll(docsByCities);
        Map<String,Integer> relevantDocsForRenker = new HashMap<>();
        if (allDocsByCities.size() > 0)
            docsForQuery.retainAll(allDocsByCities);

        for (String docNumber: docsForQuery) {
            int docLength= Integer.valueOf(docsMap.get(docNumber).get(0));
            relevantDocsForRenker.put(docNumber,docLength);
        }
        ranker = new Ranker(queryToRanker, relevantDocsForRenker, docsCount_M, avdl, postingLinesToRanker, querySynonyms, synonymsDataToRanker, synonymsPostingLines);
        ranker.rankDocs();
        this.resultForQuery = ranker.getRankedDocs();
    }

    private Set<String> getRelevantDocsForQuery(List<String> postingLines) {
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

    private  Map<String, List<Pair<String,String>>> getSynonyms(List<String> queryTerms) {
        Map<String, List<Pair<String, String>>> querySynonyms = new HashMap<>();
        try {
            for (String queryTerm: queryTerms) {
                List<Pair<String,String>> synonyms = new ArrayList<>();
                URL url = new URL("https://api.datamuse.com/words?ml=" + queryTerm.toLowerCase());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(inputStreamReader);
                String jsonString = "";
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString = jsonString + line;
                }
                br.close();
                JSONArray jsonArray = new JSONArray(jsonString);
                int maxWords = 5;
                int countWords = 1;
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (countWords > maxWords)
                        break;
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    String term = (String)obj.get("word");
                    List<Integer> termData = dictionary.get(term);
                    if (termData == null){
                        termData = dictionary.get(term.toUpperCase());
                    }
                    if (termData == null){
                        continue;
                    }
                    int pointer = termData.get(2);

                    try {
                        RandomAccessFile randomAccessFile = new RandomAccessFile(postingPath, "r");
                        randomAccessFile.seek(pointer);
                        String postingLine = randomAccessFile.readLine();
                        synonyms.add(new Pair<>((String)obj.get("word"),postingLine));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    countWords++;
                }
                if (synonyms.size() > 0 )
                    querySynonyms.put(queryTerm, synonyms);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return querySynonyms;
    }

    private void addRelevantPostingLines(List<String> postingLinesToRanker, List<String> queryTermsList, List<Pair<String, Integer>> queryToRanker){
        for (int i=0; i< queryTermsList.size(); i++) {
            String queryString = queryTermsList.get(i);
            List<Integer> termData = dictionary.get(queryString);
            if (termData == null){
                termData = dictionary.get(queryString.toUpperCase());
            }
            if (termData == null){
                continue;
            }
            int df = termData.get(0);
            int pointer = termData.get(2);
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
    }

    public void getSynonymsData(List<Pair<String, String>> synonyms, List<Pair<String, Integer>> synonymsData) {
        for (int i=0; i< synonyms.size(); i++) {
            String queryString = synonyms.get(i).getKey();
            List<Integer> termData = dictionary.get(queryString);
            if (termData == null){
                termData = dictionary.get(queryString.toUpperCase());
            }
            if (termData == null){
                continue;
            }
            int df = termData.get(0);
            Pair<String, Integer> termAndDf = new Pair<>(queryString, df);
            synonymsData.add(termAndDf);
        }
    }
}
