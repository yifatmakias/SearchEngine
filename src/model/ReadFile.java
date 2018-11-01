package model;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ReadFile {

    private Set<Document> documentSet;
    private String path;

    public ReadFile(String path) {
        this.path = path;
        documentSet = new HashSet<>();
    }

    public void fillDocumentSet(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            int lineNumber =0;

            while ((line = br.readLine()) != null){
                lineNumber++;
                if (line.contains("<DOC>")){
                    Document doc = new Document();
                    doc.setStart_line(lineNumber);
                    while (!((line = br.readLine()).equals("</DOC>"))){
                        lineNumber++;
                        if (line.contains("<DOCNO>")){
                            doc.setDocNumber(removeTags(line));
                        }
                        if (line.contains("<DATE1>")){
                            doc.setDate(removeTags(line));
                        }
                        if (line.contains("<TI>")){
                            doc.setTitle(removeTags(line));
                        }
                    }
                    doc.setFile_path(path);
                    this.documentSet.add(doc);
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private String removeTags(String line){
        line = line.replaceAll("<[^>]*>", "");
        line = line.trim();
        return line;
    }

    public Set<Document> getDocumentSet() {
        return documentSet;
    }
}
