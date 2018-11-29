package model;

import java.io.*;
import java.util.Queue;


public class Merge2Files implements Runnable{

    private String post1Path;
    private String post2Path;
    private String mergedFilePath;
    private Queue<File> pathList;

    public Merge2Files(String post1Path, String post2Path, String mergedFilePath, Queue<File> pathList) {
        this.post1Path = post1Path;
        this.post2Path = post2Path;
        this.mergedFilePath = mergedFilePath;
        this.pathList = pathList;
    }

    @Override
    public void run() {
        merge2PostingFiles();
    }

    public void merge2PostingFiles(){
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(post1Path));
            BufferedReader br2 = new BufferedReader(new FileReader(post2Path));
            File postingFile = new File(mergedFilePath);
            PrintWriter printWriter = new PrintWriter(postingFile);
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            while (line1 != null && line2 != null){
                String [] splitLine1 = line1.split("\\$");
                String [] splitLine2 = line2.split("\\$");
                String [] dicData1 = splitLine1[0].split(";");
                String [] dicData2 = splitLine2[0].split(";");
                String termLine1 = dicData1[0];
                String termLine2 = dicData2[0];
                if (termLine1.compareTo(termLine2) == 0){
                    String newMergedLine = merge2Lines(line1, line2);
                    printWriter.println(newMergedLine);
                    line1 = br1.readLine();
                    line2 = br2.readLine();
                }
                else if(termLine1.compareTo(termLine2) > 0){
                    printWriter.println(line2);
                    line2 = br2.readLine();
                }
                else {
                    printWriter.println(line1);
                    line1 = br1.readLine();
                }
            }
            if (line1 == null && line2 == null){
                printWriter.flush();
                br1.close();
                br2.close();
                printWriter.close();
                File postFile1 = new File(post1Path);
                File postFile2 = new File(post2Path);
                postFile1.delete();
                postFile2.delete();
                File newFile = new File(mergedFilePath);
                this.pathList.add(newFile);
                return;
            }

            else if(line1 != null){
                while (line1 != null){
                    printWriter.println(line1);
                    line1 = br1.readLine();
                }
            }
            else {
                while (line2 != null){
                    printWriter.println(line2);
                    line2 = br2.readLine();
                }
            }
            printWriter.flush();
            br1.close();
            br2.close();
            printWriter.close();
            File postFile1 = new File(post1Path);
            File postFile2 = new File(post2Path);
            File newFile = new File(mergedFilePath);
            postFile1.delete();
            postFile2.delete();
            this.pathList.add(newFile);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String merge2Lines(String line1, String line2){
        String mergedLine = "";
        String [] splitLine1 = line1.split("\\$");
        String [] splitLine2 = line2.split("\\$");
        String [] dicData1 = splitLine1[0].split(";");
        String [] dicData2 = splitLine2[0].split(";");
        if (line1.contains("<") || line2.contains("<")){
            System.out.println(line1);
            System.out.println(line2);
        }
        int dfLine1 = Integer.valueOf(dicData1[1]);
        int dfLine2 = Integer.valueOf(dicData2[1]);
        String mergedDf = String.valueOf(dfLine1+dfLine2);
        int tfLine1 = Integer.valueOf(dicData1[2]);
        int tfLine2 = Integer.valueOf(dicData2[2]);
        String mergedTf = String.valueOf(tfLine1+tfLine2);
        mergedLine = dicData1[0]+";"+mergedDf+";"+mergedTf+"$";
        for (int i = 1; i < splitLine1.length ; i++) {
            mergedLine = mergedLine + splitLine1[i]+"$";
        }
        for (int i = 1; i < splitLine2.length ; i++) {
            mergedLine = mergedLine + splitLine2[i]+"$";
        }
        mergedLine = mergedLine.substring(0,mergedLine.length()-1);
        return mergedLine;
    }
}
