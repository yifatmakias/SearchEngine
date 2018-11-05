package model;

import java.util.Arrays;

public class Parse {
    private String[] tokens;

    public Parse(String text) {
        this.tokens = text.split("\\s+");
    }

    public String[] getTokens() {
        return tokens;
    }

    public void setTokens(String[] tokens) {
        this.tokens = tokens;
    }

    public void parse(){

    }

    @Override
    public String toString() {
        return "Parse{" +
                "tokens=" + Arrays.toString(tokens) +
                '}';
    }
}
