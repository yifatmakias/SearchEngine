package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Doc {
    private String file_path;
    private String  docNumber;
    private String date;
    private String title;
    private String text;
    private int max_tf;
    private int uniqueWordCount;
    private String city;


    public String getCity() {
        return city;
    }

    public void setMax_tf(int max_tf) {
        this.max_tf = max_tf;
    }

    public void setUniqueWordCount(int uniqueWordCount) {
        this.uniqueWordCount = uniqueWordCount;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setText(String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return "Doc{" +
                "file_path='" + file_path + '\'' +
                ", docNumber='" + docNumber + '\'' +
                ", date=" + date +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
