package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * this class creates temp posting files.
 */
public class Indexer implements Runnable {

    private String postingPath;
    private int fileNum;
    private Map<String, Term> termsMap;

    public Indexer(String postingPath, int fileNum, Map<String, Term> termsMap) {
        this.postingPath = postingPath;
        this.fileNum = fileNum;
        this.termsMap = termsMap;
    }

    public void run() {
        createPosting();
    }

    /**
     * create temp posting file for a given terms map from parser.
     */
    private void createPosting(){
        List<String> termsByOrder = new ArrayList<>(termsMap.keySet());
        Collections.sort(termsByOrder);
        File postingFile = new File(this.postingPath+"file"+String.valueOf(fileNum));
        try {
            PrintWriter printWriter = new PrintWriter(postingFile);
            for (String term: termsByOrder) {
                String lineToWrite;
                String tfInCorpusString = String.valueOf(termsMap.get(term).getTfInCorpus());
                String dfString = String.valueOf(termsMap.get(term).getDf());
                lineToWrite = term+";"+tfInCorpusString+";"+dfString+"*";
                for (Iterator<Map.Entry<String, List<Integer>>> it = termsMap.get(term).getDocuments().entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, List<Integer>> entry = it.next();
                    String docData = entry.getKey()+"$";
                    List<Integer> listDocData = entry.getValue();

                    for (Integer val: listDocData) {
                        docData = docData + String.valueOf(val)+",";
                    }
                    docData = docData.substring(0,docData.length()-1);
                    docData = docData+"$";
                    lineToWrite = lineToWrite + docData;
                }
                lineToWrite = lineToWrite.substring(0, lineToWrite.length()-1);
                printWriter.println(lineToWrite);
            }
            printWriter.flush();
            printWriter.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
