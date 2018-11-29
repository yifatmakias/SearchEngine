package model;

import java.io.*;
import java.util.*;

public class Merge {

    private String postingPath;

    public Merge(String postingPath) {
        this.postingPath = postingPath;
    }

    public void merge(){
        try {
            File folder = new File(postingPath);
            File [] filesArr = folder.listFiles();
            BufferedReader [] bufferedReaders = new BufferedReader[filesArr.length];
            String [] lines = new String[filesArr.length];
            int numOfNullLines = 0;

            for (int i = 0; i < filesArr.length; i++) {
                File file = filesArr[i];
                bufferedReaders[i] = new BufferedReader(new FileReader(file));
            }
            PrintWriter printWriter = new PrintWriter(postingPath+"mergedPosting");
            for (int i = 0; i < bufferedReaders.length; i++) {
                if (bufferedReaders[i].readLine() != null){
                    lines[i] = bufferedReaders[i].readLine();
                }
                else {
                    numOfNullLines++;
                }
            }
            Queue<String> linesToSort = new PriorityQueue<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String [] splitline1 = o1.split("\\*");
                    String [] splitline2 = o2.split("\\*");
                    String term1 = splitline1[0];
                    String term2 = splitline2[0];
                    if (term1.compareTo(term2) > 0){
                        return 1;
                    }
                    else if (term1.compareTo(term2) < 0){
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            });
            for (int i = 0; i <lines.length ; i++) {
                if (lines[i] != null){
                    linesToSort.add(lines[i]);
                }
            }
            while (!linesToSort.isEmpty()){

                String minLine = linesToSort.remove();
                String [] splitLine1 = minLine.split("\\*");
                //String [] dicData1 = splitLine1[0].split(";");
                String termMinLine = splitLine1[0];

                List<String> termsToMerge = new ArrayList<>();
                for (int i = 0; i <lines.length ; i++) {
                    if (lines[i] != null){
                        String [] splitLine = lines[i].split("\\*");
                        //String [] dicData = splitLine[0].split(";");
                        String termLine = splitLine[0];
                        if (termMinLine.equals(termLine)){
                            termsToMerge.add(lines[i]);
                            linesToSort.remove(lines[i]);
                            lines[i] = bufferedReaders[i].readLine();
                            if (lines[i] != null)
                                linesToSort.add(lines[i]);
                        }
                    }
                }
                if (termsToMerge.size() <= 1){
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
        String [] splitLine = termsToMerge.get(0).split("\\*");
        //String [] dicData = splitLine[0].split(";");
        String termName = splitLine[0];
        mergedLine = termName+"*";
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
            String [] splitLine2 = termLine.split("\\*");
            mergedLine = mergedLine + splitLine2[1]+"$";
        }

        mergedLine = mergedLine.substring(0,mergedLine.length()-1);
        return mergedLine;
    }
}
