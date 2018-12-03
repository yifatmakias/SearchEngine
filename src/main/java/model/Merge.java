package model;

import java.io.*;
import java.util.*;

public class Merge {

    private String postingPath;
    private Map<String, Boolean> upperLowerDic;
    private boolean toStem;
    private Map<String, List<String>> cityMap;


    public Merge(String postingPath, Map<String, Boolean> upperLowerDic, boolean toStem, Map<String, List<String>> cityMap) {
        this.postingPath = postingPath;
        this.upperLowerDic = upperLowerDic;
        this.toStem = toStem;
        this.cityMap = cityMap;
    }

    public void merge(){
        try {
            File folder = new File(postingPath);
            List<File>tempFilesArr = Arrays.asList(folder.listFiles());
            for (File file: tempFilesArr) {
                if (!(file.getName().startsWith("file"))){
                    tempFilesArr.remove(file);
                }
            }
            File [] filesArr = tempFilesArr.toArray(new File [tempFilesArr.size()]);
            BufferedReader [] bufferedReaders = new BufferedReader[filesArr.length];
            String [] lines = new String[filesArr.length];

            for (int i = 0; i < filesArr.length; i++) {
                File file = filesArr[i];
                bufferedReaders[i] = new BufferedReader(new FileReader(file));
            }
            int lineNumber = 0;
            PrintWriter printWriter = new PrintWriter(postingPath+"mergedPosting");
            PrintWriter printWriterCities = new PrintWriter(postingPath+"mergedPostingCities");
            for (int i = 0; i < bufferedReaders.length; i++) {
                lines[i] = bufferedReaders[i].readLine();
            }

            Queue<String> linesToSort = new PriorityQueue<>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    String [] splitline1 = o1.split("\\*");
                    String [] splitline2 = o2.split("\\*");
                    String [] termData1 = splitline1[0].split(";");
                    String [] termData2 = splitline2[0].split(";");
                    String term1 = termData1[0];
                    String term2 = termData2[0];
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
                String [] dicData1 = splitLine1[0].split(";");
                String termMinLine = dicData1[0];
                List<String> termsToMerge = new ArrayList<>();
                for (int i = 0; i <lines.length ; i++) {
                    if (lines[i] != null){
                        String [] splitLine = lines[i].split("\\*");
                        String [] dicData = splitLine[0].split(";");
                        String termLine = dicData[0];
                        if (termMinLine.equals(termLine)){
                            termsToMerge.add(lines[i]);
                            linesToSort.remove(lines[i]);
                            lines[i] = bufferedReaders[i].readLine();
                            if (lines[i] != null){
                                linesToSort.add(lines[i]);
                            }
                        }
                    }
                }
                if (termsToMerge.size() <= 1){
                    if (upperLowerDic.containsKey(termMinLine) && !upperLowerDic.get(termMinLine)){
                        minLine = termMinLine.toUpperCase() +";"+ dicData1[1]+";"+dicData1[2]+"*"+splitLine1[1];
                    }
                    if (cityMap.containsKey(termMinLine.toUpperCase())){
                        printWriterCities.println(minLine);
                    }
                    else {
                        printWriter.println(minLine);
                    }
                }
                else {
                    String mergeLine = mergeLines(termsToMerge);
                    String [] splitLine = mergeLine.split("\\*");
                    String [] dicData = splitLine[0].split(";");
                    if (cityMap.containsKey(dicData[0].toUpperCase())){
                        printWriterCities.println(mergeLine);
                    }
                    else {
                        printWriter.println(mergeLine);
                    }
                }
            }
            for (int i = 0; i <bufferedReaders.length ; i++) {
                bufferedReaders[i].close();
                filesArr[i].delete();
            }
            printWriter.flush();
            printWriter.close();
            printWriterCities.flush();
            printWriterCities.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String mergeLines(List<String> termsToMerge){
        String mergedLine = "";
        String [] splitLine = termsToMerge.get(0).split("\\*");
        String [] dicData = splitLine[0].split(";");
        String termName = dicData[0];

        if (upperLowerDic.containsKey(termName) && !upperLowerDic.get(termName)) {
            mergedLine = termName.toUpperCase()+";";
        }
        else {
            mergedLine = termName+";";
        }
        int df = 0;
        int tf = 0;

        for (String termLine: termsToMerge) {
            String [] splitLine1 = termLine.split("\\*");
            String [] dicData1 = splitLine1[0].split(";");
            int dfLine = Integer.valueOf(dicData1[1]);
            df += dfLine;
            int tfLine1 = Integer.valueOf(dicData1[2]);
            tf += tfLine1;
        }
        mergedLine = mergedLine+df+";"+tf+"*";
        for (String termLine: termsToMerge) {
            String [] splitLine2 = termLine.split("\\*");
            mergedLine = mergedLine + splitLine2[1]+"$";
        }

        mergedLine = mergedLine.substring(0,mergedLine.length()-1);
        return mergedLine;
    }

    public long separatePosting(){
        long termsCounter = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.postingPath+"mergedPosting"));
            PrintWriter dicWriter;
            PrintWriter dicWriterToShow;
            PrintWriter postWriter;
            if (toStem){
                dicWriter = new PrintWriter(this.postingPath+"stemmedDictionaryFile");
                dicWriterToShow = new PrintWriter(this.postingPath+"stemmedDictionaryToShow");
                postWriter = new PrintWriter(this.postingPath+"stemmedPostingFile");
            }
            else {
                dicWriter = new PrintWriter(this.postingPath+"dictionaryFile");
                dicWriterToShow = new PrintWriter(this.postingPath+"dictionaryFileToShow");
                postWriter = new PrintWriter(this.postingPath+"postingFile");
            }
            String line = br.readLine();
            termsCounter++;
            long seekNum = 0;
            while (line != null){
                String [] spliteLine = line.split("\\*");
                String dicLine = spliteLine[0];
                String termString = dicLine.split(";")[0];
                String tfString = dicLine.split(";")[2];
                String postLine = spliteLine[1];
                dicLine = dicLine + ";" + String.valueOf(seekNum);
                dicWriter.println(dicLine);
                dicWriterToShow.println(termString+": "+tfString);
                postWriter.println(postLine);
                line = br.readLine();
                termsCounter++;
                seekNum += postLine.getBytes().length+2;
            }
            dicWriter.flush();
            postWriter.flush();
            dicWriter.close();
            postWriter.close();
            br.close();
            File mergedPostingFile = new File(this.postingPath+"mergedPosting");
            mergedPostingFile.delete();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return termsCounter;
    }

    public void separateCitiesPosting(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.postingPath+"mergedPostingCities"));
            PrintWriter dicWriter = new PrintWriter(this.postingPath+"citiesDictionaryFile");
            PrintWriter postWriter= new PrintWriter(this.postingPath+"citiesPostingFile");

            String line = br.readLine();
            long seekNum = 0;
            while (line != null){
                String [] spliteLine = line.split("\\*");
                String dicLine = spliteLine[0];
                String cityName = dicLine.split(";")[0];
                List<String> listCityDetails = cityMap.get(cityName.toUpperCase());
                String cityDetails;
                if (listCityDetails.size() == 3){
                    cityDetails = listCityDetails.get(0) + ";" + listCityDetails.get(1) + ";" + listCityDetails.get(2);
                    dicLine = cityName + ";" + cityDetails + ";" + String.valueOf(seekNum);
                }
                else {
                    dicLine = cityName + ";" + String.valueOf(seekNum);
                }
                String postLine = spliteLine[1];
                dicWriter.println(dicLine);
                postWriter.println(postLine);
                line = br.readLine();
                seekNum += postLine.getBytes().length+2;
            }
            dicWriter.flush();
            postWriter.flush();
            dicWriter.close();
            postWriter.close();
            br.close();
            File mergedPostingFile = new File(this.postingPath+"mergedPostingCities");
            mergedPostingFile.delete();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
