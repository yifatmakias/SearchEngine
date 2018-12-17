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
    private String docsMapPath;
    private Map<String, Doc> docsMap;

    public Searcher(String query, UploadDictionary uploadDictionary, String postingPath, boolean toStem, String docsMapPath) {
        this.query = query;
        this.parse = new Parse();
        this.uploadDictionary = uploadDictionary;
        this.postingPath = postingPath;
        this.toStem = toStem;
        this.docsMapPath = docsMapPath;
        setDocsMap();
    }

    public void setDocsMap() {
        try {
            File docsFile = new File(this.docsMapPath);
            FileInputStream fileInputStream = new FileInputStream(docsFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            this.docsMap = (Map<String, Doc>) objectInputStream.readObject();
            objectInputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

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
        for (Iterator<Map.Entry<String, Doc>> it = docsMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Doc> entry = it.next();
            Doc doc = entry.getValue();
            sumDocsLength += doc.getDocLength();
        }
        double avdl = sumDocsLength/ docsCount_M;
        List<Pair<String, Integer>> queryToRanker = new ArrayList<>();
        List<String> postingLinesToRanker = new ArrayList<>();
        Map<String, List<Integer>> dictionary = uploadDictionary.getDictionary();
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

    }
}
