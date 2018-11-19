package model;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Doc {
    private String file_path;
    private String  docNumber;
    private String date;
    private String title;
    private String text;
    private int max_tf;
    private int uniqueWordCount;
    private String city;
    private Map<Term, List<Integer>> terms;

    public Doc() {
        this.terms = new HashMap<>();
    }

    public Map<Term, List<Integer>> getTerms() {
        return terms;
    }

    public void setTerms(Term term, Integer tf, Integer line, Integer indexInLine, Integer isInTitle) {
        List<Integer> termData = new ArrayList<>();
        termData.add(0, tf);
        termData.add(1, line);
        termData.add(2, indexInLine);
        termData.add(3, isInTitle);
        this.terms.put(term, termData);
    }

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

    public void updateUniqeWordsCount(){
        uniqueWordCount = this.terms.size();
    }

    public String getTitle() {
        return title;
    }

    public void updateMaxtf(){
        //max_tf = (Collections.max(terms.values()));
        for (Iterator<HashMap.Entry<Term, List<Integer>>> it = terms.entrySet().iterator(); it.hasNext(); ) {
            HashMap.Entry<Term, List<Integer>> pair = it.next();
            if (pair.getValue().get(0) > this.max_tf){
                this.max_tf = pair.getValue().get(0);
            }
        }
    }

    public int getMax_tf() {
        return max_tf;
    }

    public int getUniqueWordCount() {
        return uniqueWordCount;
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
