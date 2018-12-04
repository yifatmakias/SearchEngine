package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * this vlass represents a term.
 */
public class Term {
    private String term;
    private int df;
    private int tfInCorpus;
    private int ruleNumber;
    // map of the documents of the term.
    // data in list - tf - in doc, isInTitle, positions in doc
    private HashMap<String, List<Integer>> documents;

    public Term(String term, int ruleNumber) {
        this.term = term;
        this.df = 0;
        this.tfInCorpus = 0;
        this.ruleNumber = ruleNumber;
        documents = new HashMap<>();
    }

    public HashMap<String, List<Integer>> getDocuments() {
        return documents;
    }

    /**
     * adding a new document and its details to the details of the set.
     */
    public void addNewDocument(String docNumber,int docLength , int inTitle, int position) {
        List<Integer> termData = new ArrayList<>();
        termData.add(1);
        //termData.add(docLength);
        termData.add(inTitle);
        termData.add(position);
        this.df++;
        this.tfInCorpus++;
        documents.put(docNumber, termData);
    }

    /**
     * set the details of an existing doc of the term.
     */
    public void setExistingDoc(String docNumber, int position){
        List<Integer> termData = documents.get(docNumber);
        termData.set(0,termData.get(0)+1);
        termData.add(position);
        this.tfInCorpus++;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getDf() {
        return df;
    }

    public int getTfInCorpus() {
        return tfInCorpus;
    }

    public int getRuleNumber() {
        return ruleNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term1 = (Term) o;
        return Objects.equals(term, term1.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.term);
    }

    @Override
    public String toString() {
        return "Term{" +
                "term='" + term + '\'' +
                '}';
    }
}
