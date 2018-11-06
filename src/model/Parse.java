package model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Parse {
    private List<String> tokens;
    private HashSet<Term> terms;

    public Parse(String text) {
        this.tokens = Arrays.asList(text.split("\\s+"));
        this.terms = new HashSet<>();
    }

    public void parse(){
        for (int i = 0; i <tokens.size() ; i++) {
            // upper and lower case letters.
            if (Character.isUpperCase(tokens.get(i).charAt(0))){
                if (!tokens.contains(tokens.get(i).toLowerCase())){
                    terms.add(new Term(tokens.get(i).toUpperCase()));
                }
                else {
                    terms.add(new Term(tokens.get(i).toLowerCase()));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Parse{" +
                "tokens=" + tokens +
                ", terms=" + terms +
                '}';
    }
}
