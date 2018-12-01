package model;


import opennlp.tools.stemmer.PorterStemmer;


public class Stemmer {
    PorterStemmer portersStemmer;

    public Stemmer() {
        this.portersStemmer = new PorterStemmer();
    }

    public String stem(String term){
        String stemed = portersStemmer.stem(term);
        return stemed;
    }
}
