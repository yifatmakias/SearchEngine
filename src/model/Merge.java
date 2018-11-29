package model;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Merge {

    private String postingPath;
    //private int postingFilesNumber;
    //private ExecutorService poolExecutor;

    public Merge(String postingPath) {
        this.postingPath = postingPath;
        //this.postingFilesNumber = postingFilesNumber;
        //this.poolExecutor = Executors.newFixedThreadPool(postingFilesNumber);
    }

    public void merge(){
        try {
            File folder = new File(postingPath);
            File [] filesArr = folder.listFiles();
            BufferedReader [] bufferedReaders = new BufferedReader[filesArr.length];
            String [] lines = new String[filesArr.length];
            //System.out.println(filesArr.length);
            //List<String> lines = new ArrayList<>();
            //long maxFileSize = 0;
            //int maxFileIndex = 0;
            int numOfNullLines = 0;

            for (int i = 0; i < filesArr.length; i++) {
                File file = filesArr[i];
                /**
                 if (file.length() > maxFileSize){
                 maxFileSize = file.length();
                 maxFileIndex = i;
                 }**/
                bufferedReaders[i] = new BufferedReader(new FileReader(file));
            }
            PrintWriter printWriter = new PrintWriter(postingPath+"mergedPosting");
            for (int i = 0; i < bufferedReaders.length; i++) {
                if (bufferedReaders[i].readLine() != null){
                    lines[i] = bufferedReaders[i].readLine();
                    if (lines[i] == null){
                        numOfNullLines++;
                    }
                }
            }
            while (numOfNullLines < lines.length){
                //System.out.println(numOfNullLines);
                List<String> TermsToSort = new ArrayList<>();
                for (int i = 0; i <lines.length ; i++) {
                    if (lines[i] != null){
                        String term = lines[i].split("\\$")[0];
                        TermsToSort.add(term);
                    }
                }
                //Collections.sort(linesToSort, Comparator.naturalOrder());
                TermsToSort.sort(Comparator.nullsLast(Comparator.naturalOrder()));
                String minLine = TermsToSort.remove(0);
                String [] splitLine1 = minLine.split("\\$");
                //String [] dicData1 = splitLine1[0].split(";");
                String termMinLine = splitLine1[0];

                List<String> termsToMerge = new ArrayList<>();
                for (int i = 0; i <lines.length ; i++) {
                    if (lines[i] != null){
                        String [] splitLine = lines[i].split("\\$");
                        //String [] dicData = splitLine[0].split(";");
                        String termLine = splitLine[0];
                        if (termMinLine.equals(termLine)){
                            termsToMerge.add(lines[i]);
                            lines[i] = bufferedReaders[i].readLine();
                            if (lines[i] == null){
                                numOfNullLines++;
                            }
                        }
                    }
                }
                if (termsToMerge.size() == 1){
                    printWriter.println(minLine);
                }
                else {
                    String mergeLine = mergeLines(termsToMerge);
                    printWriter.println(mergeLine);
                }
                //maxFileSize = maxFileSize - lines[maxFileIndex].length();
            }
            for (int i = 0; i <bufferedReaders.length ; i++) {
                bufferedReaders[i].close();
            }
            printWriter.flush();
            printWriter.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String mergeLines(List<String> termsToMerge){
        String mergedLine = "";
        String [] splitLine = termsToMerge.get(0).split("\\$");
        //String [] dicData = splitLine[0].split(";");
        String termName = splitLine[0];
        mergedLine = termName+"$";
        //int df = 0;
        //int tf = 0;
        /**
        for (String termLine: termsToMerge) {
            String [] splitLine1 = termLine.split("\\$");
            String [] dicData1 = splitLine1[0].split(";");
            int dfLine = Integer.valueOf(dicData1[1]);
            df += dfLine;
            int tfLine1 = Integer.valueOf(dicData1[2]);
            tf += tfLine1;
        }
        mergedLine = mergedLine+df+";"+tf+"$";**/

        for (String termLine: termsToMerge) {
            String [] splitLine2 = termLine.split("\\$");
            for (int i = 1; i < splitLine2.length ; i++) {
                mergedLine = mergedLine + splitLine2[i]+"$";
            }
        }

        mergedLine = mergedLine.substring(0,mergedLine.length()-1);
        return mergedLine;
    }
}
