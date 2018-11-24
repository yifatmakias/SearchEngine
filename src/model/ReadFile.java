package model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.lang.String;
import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class ReadFile {

    private Set<Doc> documentSet;
    private String path;

    public ReadFile(String path) {
        this.path = path;
        documentSet = new HashSet<>();
    }

    public void fillDocumentSet(){
        try {
            File f = new File(this.path);
            Document document = Jsoup.parse(new String(Files.readAllBytes(f.toPath())));
            Elements elements = document.getElementsByTag("DOC");
            for (Element element: elements) {
                Doc doc = new Doc();
                doc.setFile_path(this.path);
                doc.setDate(element.getElementsByTag("DATE1").text());
                doc.setTitle(element.getElementsByTag("TI").text());
                doc.setDocNumber(element.getElementsByTag("DOCNO").text().replaceAll(":","-"));
                doc.setText(element.getElementsByTag("TEXT").text());
                doc.setDocLength(doc.getText().length());
                String city = element.getElementsByAttributeValue("P","104").text().split(" ")[0].toUpperCase();
                doc.setCity(city);
                documentSet.add(doc);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public Set<Doc> getDocumentSet() {
        return documentSet;
    }
}