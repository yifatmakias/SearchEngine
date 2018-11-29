package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;

public class Indexer implements Runnable {

    //private Map<String, List<Integer>> dictionary;
    private String postingPath;
    private Boolean isStemmed;
    private int fileNum;
    Map<String, Term> termsMap;

    public Indexer(String postingPath, Boolean isStemmed, int fileNum, Map<String, Term> termsMap) {
        this.postingPath = postingPath;
        this.isStemmed = isStemmed;
        this.fileNum = fileNum;
        this.termsMap = termsMap;
    }

    @Override
    public void run() {
        createPosting();
    }

    public void createPosting(){
        List<String> termsByOrder = new ArrayList<>();
        termsByOrder.addAll(termsMap.keySet());
        Collections.sort(termsByOrder);
        File postingFile = new File(this.postingPath+"file"+String.valueOf(fileNum));
        try {
            PrintWriter printWriter = new PrintWriter(postingFile);
            for (String term: termsByOrder) {
                String lineToWrite = "";
                String tfInCorpusString = String.valueOf(termsMap.get(term).getTfInCorpus());
                String dfString = String.valueOf(termsMap.get(term).getDf());
                lineToWrite = term+"$";
                //lineToWrite = term+";"+tfInCorpusString+";"+dfString+"$";
                for (Iterator<Map.Entry<String, List<Integer>>> it = termsMap.get(term).getDocuments().entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, List<Integer>> entry = it.next();
                    String docData = entry.getKey()+"$";
                    List<Integer> listDocData = entry.getValue();
                    docData = docData + String.valueOf(listDocData.get(0));
                    /**
                    for (Integer val: listDocData) {
                        docData = docData + String.valueOf(val)+",";
                    }**/
                    //docData = docData.substring(0,docData.length()-1);
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
