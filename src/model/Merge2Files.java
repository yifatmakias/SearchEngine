package model;

import java.io.*;

public class Merge2Files implements Runnable{

    private String post1Path;
    private String post2Path;
    private String mergedFilePath;

    public Merge2Files(String post1Path, String post2Path) {
        this.post1Path = post1Path;
        this.post2Path = post2Path;
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
                String [] splitLine1 = line1.split(":");
                String [] splitLine2 = line2.split(":");
                String [] dicData1 = splitLine1[0].split(";");
                String [] dicData2 = splitLine2[0].split(";");
                String termLine1 = dicData1[0];
                String termLine2 = dicData2[0];
                if (termLine1.compareTo(termLine2) == 0){
                    String newMergedLine = merge2Lines(line1, line2);
                    printWriter.println(newMergedLine);
                }
                else if(termLine1.compareTo(termLine2) > 0){
                    printWriter.println(line2);
                    printWriter.println(line1);
                }
                else {
                    printWriter.println(line1);
                    printWriter.println(line2);
                }
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
            if (line1 == null && line2 == null)
                return;
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
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String merge2Lines(String line1, String line2){
        return null;
    }
}
