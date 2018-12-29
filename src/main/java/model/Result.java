package model;

public class Result {
    private String queryNum;
    private String docNumber;
    private String rank;

    /**
     * Result class to show in table view.
     */
    public Result(String queryNum, String docNumber, String rank) {
        this.queryNum = queryNum;
        this.docNumber = docNumber;
        this.rank = rank;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public String getRank() {
        return rank;
    }

    public String getQueryNum() {
        return queryNum;
    }
}
