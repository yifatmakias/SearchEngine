package model;

public class Result {
    private String docNumber;
    private String rank;

    public Result(String docNumber, String rank) {
        this.docNumber = docNumber;
        this.rank = rank;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public String getRank() {
        return rank;
    }
}
