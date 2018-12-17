package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Doc implements Serializable {
    private String file_path;
    private String  docNumber;
    private String date;
    private String title;
    private String text;
    private int docLength;
    private int max_tf;
    private int uniqueWordCount;
    private String city;
    private String language;
    private Set<String> termsSet;

    public Doc() {
        this.termsSet = new HashSet<>();
    }

    public String getCity() {
        return city;
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

    public void setDocLength(int docLength) {
        this.docLength = docLength;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getDocLength() {
        return docLength;
    }

    public String getTitle() {
        return title;
    }


    public int getMax_tf() {
        return max_tf;
    }

    public int getUniqueWordCount() {
        return uniqueWordCount;
    }

    public void setMax_tf(int max_tf) {
        this.max_tf = max_tf;
    }

    public void setUniqueWordCount(int uniqueWordCount) {
        this.uniqueWordCount = uniqueWordCount;
    }

    public String getFile_path() {
        return file_path;
    }

    public Set<String> getTermsString() {
        return termsSet;
    }

    public void addTermToSet(String term) {
        this.termsSet.add(term);
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
