package model;

import javafx.util.Pair;

import java.util.*;
import java.util.Map.Entry;


public class Ranker {
    private Map<String, Double> rankedDocs;
    private List<Pair<String, Integer>> query; // term string after parse and df
    private Map<String,Integer> docsToRank;
    private double docsCount_M;
    private double avdl; // the average length of the docs in corpus.
    private List<String> postingLines;
    private Map<String, List<Pair<String, String>>> querySynonyms;
    private List<Pair<String, Integer>> synonymsData;
    private List<String> synynomPostingLines;

    public Ranker(List<Pair<String, Integer>> query, Map<String,Integer> docsToRank, double docsCount_M, double avdl, List<String> postingLines,Map<String, List<Pair<String, String>>> querySynonyms, List<Pair<String, Integer>> synonymsData, List<String> synynomPostingLines) {
        this.query = query;
        this.docsToRank = docsToRank;
        this.rankedDocs = new LinkedHashMap<>();
        this.docsCount_M = docsCount_M;
        this.avdl = avdl;
        this.postingLines = postingLines;
        this.querySynonyms = querySynonyms;
        this.synonymsData = synonymsData;
        this.synynomPostingLines = synynomPostingLines;
        this.query.addAll(synonymsData);
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
        Map<String, Double> tempRankedDocs = new LinkedHashMap<>();

        for (Iterator<Entry<String, Integer>> it = docsToRank.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Integer> entry = it.next();
            String docNumber= entry.getKey();
            double BM25Factor = 0;
            double inTitleFactor = 0;
            double positionFactor = 0;
            double docLength = entry.getValue(); // |d|
            for (int i = 0; i < query.size(); i++) {
                String termInQuery = query.get(i).getKey();
                int countTermInQuery = countTermInQuery(termInQuery); // c(w,q)
                double termDf = query.get(i).getValue();
                List<Integer> docData = parsedPostingLines.get(i).get(docNumber);
                if (docData == null)
                    continue;
                int termTf = docData.get(0);
                int isInTitle = docData.get(1);
                int firstPosition = docData.get(2);
                /**
                if (querySynonyms != null && querySynonyms.containsKey(termInQuery)){
                    termDf += getDfToAdd(querySynonyms.get(termInQuery));
                }
                if (querySynonyms != null && querySynonyms.containsKey(termInQuery)){
                    List<Pair<String, String>> synonymPairsForQueryTerm = querySynonyms.get(termInQuery);
                    for (Pair<String, String> pair: synonymPairsForQueryTerm) {
                        String PostingLine = pair.getValue();
                        String [] splitePostingLine = PostingLine.split("\\$");
                        for (int j = 0; j < splitePostingLine.length - 1 ; j+=2) {
                            if (splitePostingLine[j].equals(docNumber)){
                                String [] docDataStrings = splitePostingLine[j+1].split(",");
                                termTf += Integer.valueOf(docDataStrings[0]);
                            }
                        }
                    }
                }**/
                // the formula for BM25 factor - for the specific doc.
                BM25Factor += (countTermInQuery * (k+1)* termTf * Math.log10((docsCount_M+1)/termDf)) / (termTf * k * (1- b + b * (docLength/avdl)));
                // the formula for inTitle factor - for the specific doc.
                inTitleFactor += isInTitle;
                // the formula for position factor - for the specific doc.
                positionFactor+= (docLength - firstPosition) / docLength;
            }
            double rank = BM25FactorWeight * BM25Factor + inTitleFactorWeight * inTitleFactor + positionFactorWeight * positionFactor;
            tempRankedDocs.put(docNumber, rank);
        }
        tempRankedDocs.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> this.rankedDocs.put(x.getKey(), x.getValue()));
        this.rankedDocs = getfirstFiftyDocs(rankedDocs);
    }

    private int getDfToAdd(List<Pair<String, String>> synonymsList) {
        int dfToAdd = 0;
        for (Pair<String, String> synonymPair: synonymsList) {
            String synonym = synonymPair.getKey();
            for (Pair<String, Integer> synonymData: this.synonymsData) {
                if (synonymData.getKey().equals(synonym)){
                    dfToAdd += synonymData.getValue();
                }
            }
        }
        return dfToAdd;
    }

    private List<Map<String, List<Integer>>> parsePostingLines(){
        List<String> allPostingLines = new ArrayList<>();
        allPostingLines.addAll(postingLines);
        allPostingLines.addAll(synynomPostingLines);
        List<Map<String, List<Integer>>> parsedPostingLines = new ArrayList<>();
        for (String postingLine: allPostingLines){
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

    private Map<String, Double> getfirstFiftyDocs(Map<String, Double> sortedDocs) {
        Map<String, Double> firstFifty = new LinkedHashMap<>();
        int maxDocs = 50;
        int countDocs = 1;

        for (Map.Entry<String, Double> entry: sortedDocs.entrySet()){
            if (countDocs > maxDocs)
                break;
            if (entry.getValue() != 0){
                firstFifty.put(entry.getKey(), entry.getValue());
                countDocs++;
            }
        }
        return firstFifty;
    }
}
