package model;


import opennlp.tools.stemmer.PorterStemmer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Stemmer {
    Set<Term> stemmedTerms;
    PorterStemmer portersStemmer;

    public Stemmer() {
        this.stemmedTerms = new HashSet<>();
        this.portersStemmer = new PorterStemmer();
    }

    public Term stem(Term term){
        String stemed = portersStemmer.stem(term.getTerm());
        Term stemmedTerm = new Term(stemed);
        return stemmedTerm;
    }

    public Set<Term> getTerms() {
        return stemmedTerms;
    }
}
