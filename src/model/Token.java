package model;

public class Token {

    private String token;
    private Boolean isTermed = false;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Boolean getTermed() {
        return isTermed;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTermed(Boolean termed) {
        isTermed = termed;
    }
}
