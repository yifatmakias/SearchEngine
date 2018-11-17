package model;

import java.util.Objects;

public class Term {
    private String term;


    public Term(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
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
                '}'+'\n';
    }
}
