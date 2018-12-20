package model;

import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Ranker {
    private Map<String, Double> rankedDocs;
    private List<Pair<String, Integer>> query; // term string after parse and df
    private Map<String,Integer> docsToRank;
    private double docsCount_M;
    private double avdl; // the average length of the docs in corpus.
    private List<String> postingLines;

    public Ranker(List<Pair<String, Integer>> query, Map<String,Integer> docsToRank, double docsCount_M, double avdl, List<String> postingLines) {
        this.query = query;
        this.docsToRank = docsToRank;
        this.rankedDocs = new HashMap<>();
        this.docsCount_M = docsCount_M;
        this.avdl = avdl;
        this.postingLines = postingLines;
    }

    public Map<String, Double> getRankedDocs() {
        return rankedDocs;
    }

    public int countTermInQuery(String term) {
        int result = 0;
        for (Pair<String, Integer> wordPair: query) {
            if (wordPair.getKey().equals(term)){
                result++;
            }
        }
        return result;
    }

    public void rankDocs() {
        List<Map<String, List<Integer>>> parsedPostingLines = parsePostingLines();
        double BM25FactorWeight = 0.6;
        double inTitleFactorWeight = 0.2;
        double positionFactorWeight = 0.2;
        int k = 2;
        double b = 0.75;

        for (Iterator<Map.Entry<String, Integer>> it = docsToRank.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            String docNumber= entry.getKey();
            double BM25Factor = 0;
            double inTitleFactor = 0;
            double positionFactor = 0;
            double docLength = entry.getValue();; // |d|
            for (int i = 0; i < query.size(); i++) {
                String termInQuery = query.get(i).getKey();
                int countTermInQuery = countTermInQuery(termInQuery); // c(w,q)
                double termDf = query.get(i).getValue();
                List<Integer> docData = parsedPostingLines.get(i).get(docNumber);
                int termTf = docData.get(0);
                int isInTitle = docData.get(1);
                int firstPosition = docData.get(2);
                // the formula for BM25 factor - for the specific doc.
                BM25Factor += (countTermInQuery * (k+1)* termTf * Math.log10((docsCount_M+1)/termDf)) / (termTf * k * (1- b + b * (docLength/avdl)));
                // the formula for inTitle factor - for the specific doc.
                inTitleFactor += isInTitle;
                // the formula for position factor - for the specific doc.
                positionFactor+= (docLength - firstPosition) / docLength;
            }
            double rank = BM25FactorWeight * BM25Factor + inTitleFactorWeight * inTitleFactor + positionFactorWeight * positionFactor;
            this.rankedDocs.put(docNumber, rank);
        }
        this.rankedDocs = rankedDocs.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public List<Map<String, List<Integer>>> parsePostingLines(){
        List<Map<String, List<Integer>>> parsedPostingLines = new ArrayList<>();
        for (String postingLine: postingLines){
            String [] splitLine = postingLine.split("\\$");
            Map<String, List<Integer>> docsMap = new HashMap<>();
            for (int i = 0; i < splitLine.length - 1 ; i+=2) {
                String docNumber = splitLine[i];
                String [] docDataStrings = splitLine[i+1].split(",");
                List<Integer> docsData = new ArrayList<>();
                for (String docData: docDataStrings) {
                    docsData.add(Integer.valueOf(docData));
                }
                docsMap.put(docNumber, docsData);
            }
            parsedPostingLines.add(docsMap);
        }
        return parsedPostingLines;
    }
}
