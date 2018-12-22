package model;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Parse {
    private List<String> tokens;
    private Map<String, Term> terms;
    private Set<String> stopWords;
    private Map<String, String> months;
    private Stemmer stemmer;
    private Map<String, Boolean> upperLowerDic;


    public Parse(Set<String> stopWords, Map<String, Boolean> upperLowerDic) {
        this.terms = new HashMap<>();
        this.stopWords = stopWords;
        this.stemmer = new Stemmer();
        fillMonthDic();
        this.upperLowerDic = upperLowerDic;
    }

    public Parse() {
        this.terms = new HashMap<>();
        this.stemmer = new Stemmer();
        fillMonthDic();
    }

    public Parse(Set<String> stopWords) {
        this.terms = new HashMap<>();
        this.stemmer = new Stemmer();
        this.stopWords = stopWords;
        fillMonthDic();
    }

    /**
     * parse a given string or a text of the given doc.
     * @param doc         - document (optional)
     * @param textToParse - text to parse (optional)
     * @param toStem      - a boolean tha says if to stem the text or not.
     */
    public void parse(Doc doc, String textToParse, Boolean toStem) {
        String text;
        if (doc != null) {
            text = doc.getText();
        } else {
            text = textToParse;
        }
        int indexInText = 0;
        this.tokens = new ArrayList<>(Arrays.asList(text.split("([^0-9a-zA-Z%]+)?\\s+([^$0-9a-zA-Z]+)?")));
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);


            // remove special characters
            token = replaceChars(token, "*", "");
            token = replaceChars(token, ";", "");
            token = replaceChars(token, "(", "");
            token = replaceChars(token, ")", "");
            token = replaceChars(token, "\"", "");
            token = replaceChars(token, "|", "");
            token = replaceChars(token, "\'", "");
            token = replaceChars(token, "]", "");
            token = replaceChars(token, "[", "");
            token = replaceChars(token, "&", "");
            token = replaceChars(token, "<", "");
            token = replaceChars(token, ">", "");
            token = replaceChars(token, "?", "");
            token = token.replaceAll("--", "-");


            token = token.replaceAll("[^\\p{Print}\\t\\n]", "");


            if (token.length() <= 1) {
                continue;
            }

            // dealing with percentage - 1
            if (token.equals("percent") || token.equals("percentage")) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    String stringTerm = tokens.get(i - 1) + "%";
                    addTerm(stringTerm, doc, toStem, indexInText, 1);
                }
            }

            // prices - 2
            else if (token.equals("Dollars")) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    Double price = Double.parseDouble(tokens.get(i - 1).replace(",", ""));
                    if (price < 1000000) {
                        String stringTerm = tokens.get(i - 1) + " Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText, 2);
                    } else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")) {
                            priceStr = priceStr.replace(".0", "");
                        }
                        String stringTerm = priceStr + " M" + " Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText, 2);
                    }
                } else {
                    if (i - 1 >= 0 && isFraction(tokens.get(i - 1))) {
                        if (i - 2 >= 0 && isNumeric(tokens.get(i - 2))) {
                            Double price = Double.parseDouble(tokens.get(i - 2).replace(",", ""));
                            if (price < 1000000) {
                                String stringTerm = tokens.get(i - 2) + " " + tokens.get(i - 1) + " Dollars";
                                addTerm(stringTerm, doc, toStem, indexInText, 2);
                            }
                        }
                    }
                    if (i - 1 >= 0 && tokens.get(i - 1).equals("m")) {
                        if (i - 2 >= 0 && isNumeric(tokens.get(i - 2))) {
                            String stringTerm = tokens.get(i - 2) + " M" + " Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText, 2);
                        }
                    }
                    if (i - 1 >= 0 && tokens.get(i - 1).equals("bn")) {
                        if (i - 2 >= 0 && isNumeric(tokens.get(i - 2))) {
                            String stringTerm = tokens.get(i - 2) + "000 M" + " Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText, 2);
                        }
                    }
                }
            } else if (token.contains("$")) {
                String newToken = token.replace("$", "");
                if (i + 1 < tokens.size() && tokens.get(i + 1).equals("million")) {
                    String stringTerm = newToken + " M" + " Dollars";
                    addTerm(stringTerm, doc, toStem, indexInText, 2);
                }
                if (i + 1 < tokens.size() && tokens.get(i + 1).equals("billion")) {
                    String stringTerm = newToken + "000 M" + " Dollars";
                    addTerm(stringTerm, doc, toStem, indexInText, 2);
                } else if (isNumeric(newToken)) {
                    Double price = Double.parseDouble(newToken.replace(",", ""));
                    if (price < 1000000) {
                        String stringTerm = newToken + " Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText, 2);
                    } else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")) {
                            priceStr = priceStr.replace(".0", "");
                        }
                        String stringTerm = priceStr + " M" + " Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText, 2);
                    }
                }
            } else if (token.equals("dollars")) {
                if (i - 1 >= 0 && tokens.get(i - 1).equals("U.S.")) {
                    if (i - 2 >= 0 && tokens.get(i - 2).equals("million")) {
                        if (i - 3 >= 0 && isNumeric(tokens.get(i - 3))) {
                            String stringTerm = tokens.get(i - 3) + " M" + " Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText, 2);
                        }
                    } else if (i - 2 >= 0 && tokens.get(i - 2).equals("billion")) {
                        if (i - 3 >= 0 && isNumeric(tokens.get(i - 3))) {
                            String stringTerm = tokens.get(i - 3) + "000 M" + " Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText, 2);
                        }
                    } else if (i - 2 >= 0 && tokens.get(i - 2).equals("trillion")) {
                        if (i - 3 >= 0 && isNumeric(tokens.get(i - 3))) {
                            String stringTerm = tokens.get(i - 3) + "000000 M" + " Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText, 2);
                        }
                    }
                }
            }
            // dates - 3
            else if (months.containsKey(token)) {
                if (i - 1 >= 0 && tokens.get(i - 1).matches("[0-9]+")) {
                    int date = Integer.valueOf(tokens.get(i - 1));
                    if (date <= 31) {
                        String stringTerm = months.get(token) + "-" + date;
                        addTerm(stringTerm, doc, toStem, indexInText, 3);
                    }
                }
                if (i + 1 < tokens.size() && tokens.get(i + 1).matches("[0-9]+")) {
                    int date = Integer.parseInt(tokens.get(i + 1));
                    if (date <= 31) {
                        if (date < 10) {
                            String stringTerm = months.get(token) + "-0" + date;
                            addTerm(stringTerm, doc, toStem, indexInText, 3);
                        } else {
                            String stringTerm = months.get(token) + "-" + date;
                            addTerm(stringTerm, doc, toStem, indexInText, 3);
                        }
                    } else {
                        String stringTerm = date + "-" + months.get(token);
                        addTerm(stringTerm, doc, toStem, indexInText, 3);
                    }
                }
            }
            // Numbers - 4
            else if (token.equals("Thousand")) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    String stringTerm = tokens.get(i - 1) + "K";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                }
            } else if (token.equals("Million")) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    String stringTerm = tokens.get(i - 1) + "M";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                }
            } else if (token.equals("Billion")) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    String stringTerm = tokens.get(i - 1) + "B";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                }
            } else if (token.equals("Trillion")) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    String stringTerm = tokens.get(i - 1) + "00B";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                }
            } else if (isNumeric(token)) {
                Double newToken = Double.parseDouble(token.replace(",", ""));
                if (newToken >= 1000 && newToken < 1000000) {
                    newToken = newToken / 1000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")) {
                        tokenStr = tokenStr.replace(".0", "");
                    } else {
                        //tokenStr = convertBigDecimalNum(tokenStr);
                    }
                    String stringTerm = tokenStr + "K";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                } else if (newToken >= 1000000 && newToken < 1000000000) {
                    newToken = newToken / 1000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")) {
                        tokenStr = tokenStr.replace(".0", "");
                    } else {
                        //tokenStr = convertBigDecimalNum(tokenStr);
                    }
                    String stringTerm = tokenStr + "M";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                } else if (newToken >= 1000000000) {
                    newToken = newToken / 1000000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")) {
                        tokenStr = tokenStr.replace(".0", "");
                    } else {
                        //tokenStr = convertBigDecimalNum(tokenStr);
                    }
                    String stringTerm = tokenStr + "B";
                    addTerm(stringTerm, doc, toStem, indexInText, 4);
                } else {
                    if (i + 1 < tokens.size() && !(tokens.get(i + 1).equals("Million") || tokens.get(i + 1).equals("Billion") || tokens.get(i + 1).equals("Trillion") || tokens.get(i + 1).equals("Thousand"))) {
                        /**if (token.contains(".")) {
                         token = convertSmallDecimalNum(token);
                         }**/
                        String stringTerm = token;
                        addTerm(stringTerm, doc, toStem, indexInText, 4);
                    }
                }
            } else if (isFraction(token)) {
                if (i - 1 >= 0 && isNumeric(tokens.get(i - 1))) {
                    double newToken;
                    if (tokens.get(i - 1).contains(","))
                        newToken = Double.parseDouble(tokens.get(i - 1).replace(",", ""));
                    else
                        newToken = Double.parseDouble(tokens.get(i - 1));
                    if (newToken < 1000) {
                        String stringTerm = tokens.get(i - 1) + " " + token;
                        addTerm(stringTerm, doc, toStem, indexInText, 4);
                    }
                }
            } else if (token.equals("between") || token.equals("Between")) {
                if (i + 1 < tokens.size() && isNumeric(tokens.get(i + 1))) {
                    if (i + 2 < tokens.size() && tokens.get(i + 2).equals("and")) {
                        if (i + 3 < tokens.size() && isNumeric(tokens.get(i + 3))) {
                            String stringTerm = token + " " + tokens.get(i + 1) + " " + tokens.get(i + 2) + " " + tokens.get(i + 3);
                            addTerm(stringTerm, doc, toStem, indexInText, 4);
                        }
                    }
                }
            } else if (token.contains("/")) {
                String[] splitTokens = token.split("/");
                for (String splitedToken : splitTokens) {
                    addTerm(splitedToken, doc, toStem, indexInText, 5);
                }
            }
            // Evrey thing else - 5
            else {
                if (token.charAt(0) == '"' || token.charAt(0) == '\'' || token.charAt(0) == '.' || token.charAt(0) == '|' || token.charAt(0) == ',' || token.charAt(0) == '_' || token.charAt(0) == '+') {
                    token = token.substring(1);
                }
                if (token.charAt(token.length() - 1) == '"' || token.charAt(token.length() - 1) == '\'' || token.charAt(token.length() - 1) == '.' || token.charAt(token.length() - 1) == '|' || token.charAt(token.length() - 1) == '_') {
                    token = token.substring(0, token.length() - 1);
                }

                if (token.length() > 1 && token.charAt(0) == '-' && token.charAt(1) == '-') {
                    token = token.substring(2);
                }

                if (token.length() <= 1)
                    continue;

                addTerm(token, doc, toStem, indexInText, 5);
            }
            indexInText++;
        }
    }

    /**
     * convert big decimal numbers - return 2 digits after the point.
     */
    private String convertBigDecimalNum(String number) {
        String[] numSplited = number.split("\\.");
        String beforePoint = numSplited[0];
        String afterPoint = numSplited[1];
        if (afterPoint.length() >= 3) {
            if (Character.getNumericValue(afterPoint.charAt(2)) < 5) {
                return beforePoint + "." + afterPoint.substring(0, 2);
            } else {
                int roundUp = Character.getNumericValue(afterPoint.charAt(1)) + 1;
                return beforePoint + "." + afterPoint.substring(0, 1) + String.valueOf(roundUp);
            }
        } else {
            return number;
        }
    }

    /**
     * convert small decimal numbers - return 5 digits after the point.
     */
    private String convertSmallDecimalNum(String number) {
        String[] numSplited = number.split("\\.");
        String beforePoint = numSplited[0];
        String afterPoint = numSplited[1];
        if (afterPoint.length() >= 6) {
            if (Character.getNumericValue(afterPoint.charAt(5)) < 5) {
                return beforePoint + "." + afterPoint.substring(0, 5);
            } else {
                int roundUp = Character.getNumericValue(afterPoint.charAt(4)) + 1;
                return beforePoint + "." + afterPoint.substring(0, 4) + String.valueOf(roundUp);
            }
        } else {
            return number;
        }
    }

    /**
     * remove stop words.
     */
    public void removeStopWords() {
        for (Iterator<Map.Entry<String, Term>> it = terms.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Term> entry = it.next();
            Term term = entry.getValue();
            if (stopWords.contains(term.getTerm())) {
                it.remove();
            }
        }
    }

    /**
     * adds a new term to the terms map or updating details for a term that is already in the map.
     */
    private void addTerm(String stringTerm, Doc doc, Boolean toStem, int indexInText, int rule) {
        Term term;
        // check if toStemm is true, and change stringTerm accordingly.
        if (toStem) {
            stringTerm = stemmer.stem(stringTerm);
        }
        // if term came from rule 5  - change it to lower case.
        if (rule == 5) {
            if (terms.containsKey(stringTerm.toLowerCase())) {
                term = terms.get(stringTerm.toLowerCase());
            } else {
                term = new Term(stringTerm.toLowerCase(), rule);
            }

        } else { // if term came from rules 1-4 dont do lower case.
            if (terms.containsKey(stringTerm)) {
                term = terms.get(stringTerm);
            } else {
                term = new Term(stringTerm, rule);
            }
        }
        // only for query
        if (doc == null) {
            if (toStem) {
                term.setTerm(stemmer.stem(term.getTerm()));
            }
            terms.put(term.getTerm(), term);
        } else {
            // update upperLowerDic
            if (upperLowerDic.containsKey(term.getTerm())) {
                char firstLetter;
                int i = 0;
                while (i < stringTerm.length() - 1 && !Character.isLetter(stringTerm.charAt(i))) {
                    i++;
                }
                if (stringTerm.length() > 0) {
                    firstLetter = stringTerm.charAt(i);
                    if (stringTerm.length() >= 1 && Character.isLowerCase(firstLetter)) {
                        upperLowerDic.replace(term.getTerm(), true);
                    }
                }
            } else {
                char firstLetter;
                int i = 0;
                while (i < stringTerm.length() - 1 && !Character.isLetter(stringTerm.charAt(i))) {
                    i++;
                }
                if (stringTerm.length() > 0) {
                    firstLetter = stringTerm.charAt(i);
                    if (stringTerm.length() >= 1 && Character.isLowerCase(firstLetter)) {
                        upperLowerDic.replace(term.getTerm(), true);
                    } else {
                        upperLowerDic.put(stringTerm.toLowerCase(), false);
                        doc.addTermToSet(term.getTerm());
                    }
                }
            }
            // update details of the term.
            int isInTitle = 0;
            if (doc.getTitle().contains(term.getTerm().toUpperCase()) || doc.getTitle().contains(term.getTerm().toLowerCase())) {
                isInTitle = 1;
            }
            if (terms.containsKey(term.getTerm()) && terms.get(term.getTerm()).getDocuments().containsKey(doc.getDocNumber())) {
                terms.get(term.getTerm()).setExistingDoc(doc.getDocNumber(), indexInText);
                int currentTf = terms.get(term.getTerm()).getDocuments().get(doc.getDocNumber()).get(0);
                if (currentTf > doc.getMax_tf()) {
                    doc.setMax_tf(currentTf);
                }
            } else {
                terms.put(term.getTerm(), term);
                terms.get(term.getTerm()).addNewDocument(doc.getDocNumber(), doc.getDocLength(), isInTitle, indexInText);
                int currentTf = terms.get(term.getTerm()).getDocuments().get(doc.getDocNumber()).get(0);
                if (currentTf > doc.getMax_tf()) {
                    doc.setMax_tf(currentTf);
                }
                doc.setUniqueWordCount(doc.getUniqueWordCount() + 1);
            }
        }
    }

    public Map<String, Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return "Parse{" +
                "tokens=" + tokens +
                ", terms=" + terms +
                '}';
    }

    /**
     * @param str
     * @return true if the given string is a number.
     */
    public boolean isNumeric(String str) {
        return str.matches("-?\\d+(,\\d+)*(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    /**
     * @param str
     * @return true if the string is a fraction number.
     */
    public boolean isFraction(String str) {
        return str.matches("\\d+/\\d+|\\.\\d+");
    }

    /**
     * fills months map for parse use.
     */
    private void fillMonthDic() {
        this.months = new HashMap<>();
        String[] monthArr = {"JANUARY", "january", "January", "jan", "JAN", "Jan", "FEBRUARY", "february", "February", "feb", "FEB", "Feb", "MARCH", "march", "March", "mar", "MAR", "Mar", "APRIL", "april", "April", "apr", "APR", "Apr", "MAY", "may", "May", "JUNE", "june", "June", "jun", "JUN", "Jun", "JULY", "july", "July", "jul", "JUL", "Jul", "AUGUST", "august", "August", "aug", "AUG", "Aug", "SEPTEMBER", "september", "September", "sep", "SEP", "Sep", "OCTOBER", "october", "October", "oct", "OCT", "Oct", "NOVEMBER", "november", "November", "nov", "NOV", "Nov", "DECEMBER", "december", "December", "dec", "DEC", "Dec"};
        String[] numsArr = {"01", "01", "01", "01", "01", "01", "02", "02", "02", "02", "02", "02", "03", "03", "03", "03", "03", "03", "04", "04", "04", "04", "04", "04", "05", "05", "05", "06", "06", "06", "06", "06", "06", "07", "07", "07", "07", "07", "07", "08", "08", "08", "08", "08", "08", "09", "09", "09", "09", "09", "09", "10", "10", "10", "10", "10", "10", "11", "11", "11", "11", "11", "11", "12", "12", "12", "12", "12", "12"};
        for (int i = 0; i < monthArr.length; i++) {
            this.months.put(monthArr[i], numsArr[i]);
        }
    }
}
