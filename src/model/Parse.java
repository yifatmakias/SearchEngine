package model;

import java.util.*;

public class Parse {
    private List<String> tokens;
    private Map<String, Term> terms;
    private Set<String> stopWords;
    private Map<String, String> months;
    private Map<String, Term> upperCaseWords;
    private Stemmer stemmer;


    public Parse(Set<String> stopWords) {
        this.terms = new HashMap<>();
        this.stopWords = stopWords;
        this.upperCaseWords = new HashMap<>();
        this.stemmer = new Stemmer();
        fillMonthDic();
    }

    public void parse(Doc doc, String textToParse, Boolean toStem){
        String text;
        if (doc != null){
            text = doc.getText();
        }
        else {
            text = textToParse;
        }
        int indexInText = 0;
        this.tokens = new ArrayList<>(Arrays.asList(text.split("([^0-9a-zA-Z%]+)?\\s+([^$0-9a-zA-Z]+)?")));
        //System.out.println(tokens.toString());
        for (int i = 0; i <tokens.size() ; i++) {
            String token = tokens.get(i);

            if (token.length() <= 1){
                continue;
            }

            // upper and lower case letters.
            if (Character.isUpperCase(token.charAt(0))){
                this.upperCaseWords.put(token.toUpperCase(), new Term(token.toUpperCase()));
            }

            // dealing with percentage.
            if (token.equals("percent") || token.equals("percentage")){
                if (i-1 >=0 && isNumeric(tokens.get(i-1))){
                    String stringTerm = tokens.get(i-1)+"%";
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
            }

            // prices
            else if (token.equals("Dollars")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Double price = Double.parseDouble(tokens.get(i-1).replace(",",""));
                    if (price < 1000000){
                        String stringTerm = tokens.get(i-1)+" Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText);
                    }
                    else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")){
                            priceStr = priceStr.replace(".0", "");
                        }
                        String stringTerm = priceStr+" M"+" Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText);
                    }
                }
                else {
                    if (i-1 >= 0 && isFraction(tokens.get(i-1))){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            Double price = Double.parseDouble(tokens.get(i-2).replace(",",""));
                            if (price < 1000000){
                                String stringTerm = tokens.get(i-2)+" "+tokens.get(i-1)+" Dollars";
                                addTerm(stringTerm, doc, toStem, indexInText);
                            }
                        }
                    }
                    if (i-1 >=0 && tokens.get(i-1).equals("m")){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            String stringTerm = tokens.get(i-2)+" M"+" Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText);
                        }
                    }
                    if (i-1 >=0 && tokens.get(i-1).equals("bn")){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            String stringTerm = tokens.get(i-2)+"000 M"+" Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText);
                        }
                    }
                }
            }
            else if (token.contains("$")){
                String newToken = token.replace("$","");
                if (i+1 < tokens.size() && tokens.get(i+1).equals("million")){
                    String stringTerm = newToken+" M"+" Dollars";
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
                if (i+1 < tokens.size() && tokens.get(i+1).equals("billion")){
                    String stringTerm = newToken+"000 M"+" Dollars";
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
                else if (isNumeric(newToken)){
                    Double price = Double.parseDouble(newToken.replace(",",""));
                    if (price < 1000000){
                        String stringTerm = newToken+" Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText);
                    }
                    else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")){
                            priceStr = priceStr.replace(".0", "");
                        }
                        String stringTerm = priceStr+" M"+" Dollars";
                        addTerm(stringTerm, doc, toStem, indexInText);
                    }
                }
            }

            else if (token.equals("dollars")){
                if (i-1 >=0 && tokens.get(i-1).equals("U.S.")){
                    if (i-2 >=0 && tokens.get(i-2).equals("million")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            String stringTerm = tokens.get(i-3)+" M"+" Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText);                        }
                    }
                    else if (i-2 >=0 && tokens.get(i-2).equals("billion")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            String stringTerm = tokens.get(i-3)+"000 M"+" Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText);                           }
                    }
                    else if (i-2 >=0 && tokens.get(i-2).equals("trillion")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            String stringTerm = tokens.get(i-3)+"000000 M"+" Dollars";
                            addTerm(stringTerm, doc, toStem, indexInText);
                        }
                    }
                }
            }
            // dates
            else if (months.containsKey(token)){
                if (i-1 >= 0 && tokens.get(i-1).matches("[0-9]+")){
                    int date = Integer.valueOf(tokens.get(i-1));
                    if (date <= 31){
                        String stringTerm = months.get(token)+"-"+date;
                        addTerm(stringTerm, doc, toStem, indexInText);                    }
                }
                if (i+1 < tokens.size() && tokens.get(i+1).matches("[0-9]+")){
                    int date = Integer.parseInt(tokens.get(i+1));
                    if (date <= 31){
                        if (date < 10){
                            String stringTerm = months.get(token)+"-0"+date;
                            addTerm(stringTerm, doc, toStem, indexInText);                             }
                        else {
                            String stringTerm = months.get(token)+"-"+date;
                            addTerm(stringTerm, doc, toStem, indexInText);                            }
                    }
                    else {
                        String stringTerm = date+"-"+months.get(token);
                        addTerm(stringTerm, doc, toStem, indexInText);                     }
                }
            }
            else if (token.equals("between") || token.equals("Between")){
                if (i+1 < tokens.size() && isNumeric(tokens.get(i+1))){
                    if (i+2 < tokens.size() && tokens.get(i+2).equals("and")){
                        if (i+3 < tokens.size() && isNumeric(tokens.get(i+3))){
                            String stringTerm = token+" "+tokens.get(i+1)+" "+tokens.get(i+2)+" "+tokens.get(i+3);
                            addTerm(stringTerm, doc, toStem, indexInText);                           }
                    }
                }
            }
            // Numbers
            else if (token.equals("Thousand")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    String stringTerm = tokens.get(i-1)+"K";
                    addTerm(stringTerm, doc, toStem, indexInText);                 }
            }
            else if (token.equals("Million")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    String stringTerm = tokens.get(i-1)+"M";
                    addTerm(stringTerm, doc, toStem, indexInText);                   }
            }
            else if (token.equals("Billion")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    String stringTerm = tokens.get(i-1)+"B";
                    addTerm(stringTerm, doc, toStem, indexInText);                }
            }
            else if (token.equals("Trillion")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    String stringTerm = tokens.get(i-1)+"00B";
                    addTerm(stringTerm, doc, toStem, indexInText);                    }
            }
            else if (isNumeric(token)){
                Double newToken = Double.parseDouble(token.replace(",",""));
                if (newToken >= 1000 && newToken < 1000000){
                    newToken = newToken / 1000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    else {
                       tokenStr = convertBigDecimalNum(tokenStr);
                    }
                    String stringTerm = tokenStr+"K";
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
                else if (newToken >= 1000000 && newToken < 1000000000){
                    newToken = newToken / 1000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    else {
                        tokenStr = convertBigDecimalNum(tokenStr);
                    }
                    String stringTerm = tokenStr+"M";
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
                else if (newToken >= 1000000000){
                    newToken = newToken / 1000000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    else {
                        tokenStr = convertBigDecimalNum(tokenStr);
                    }
                    String stringTerm = tokenStr+"B";
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
                else {
                    if (i+1 < tokens.size() && !(tokens.get(i+1).equals("Million") || tokens.get(i+1).equals("Billion") || tokens.get(i+1).equals("Trillion") || tokens.get(i+1).equals("Thousand"))){
                        if (token.contains(".")){
                            token = convertSmallDecimalNum(token);
                        }
                        String stringTerm = token;
                        addTerm(stringTerm, doc, toStem, indexInText);
                    }
                }
            }
            else if (isFraction(token)){
                if(i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Double newToken;
                    if (tokens.get(i-1).contains(","))
                        newToken = Double.parseDouble(tokens.get(i-1).replace(",",""));
                    else
                        newToken = Double.parseDouble(tokens.get(i-1));
                    if (newToken < 1000){
                        String stringTerm = tokens.get(i-1)+" "+token;
                        addTerm(stringTerm, doc, toStem, indexInText);
                    }
                }
            }
            else if (token.contains("/")){
                String [] splitTokens = token.split("/");
                for (String splitedToken: splitTokens) {
                    String stringTerm = splitedToken.toLowerCase();
                    addTerm(stringTerm, doc, toStem, indexInText);
                }
            }
            // Evrey thing else
            else {
                token = token.toLowerCase().replaceAll(";","");
                if (token.charAt(0) == '"' || token.charAt(0) == '\''){
                    token = token.substring(1);
                }
                if (token.charAt(token.length()-1) == '"' || token.charAt(token.length()-1) == '\''){
                    token = token.substring(0, token.length()-1);
                }
                String stringTerm = token.toLowerCase().replaceAll(";","");
                addTerm(stringTerm, doc, toStem, indexInText);
            }
           indexInText++;
        }
    }

    public String convertBigDecimalNum(String number){
        String [] numSplited = number.split("\\.");
        String beforePoint = numSplited[0];
        String afterPoint = numSplited[1];
        if (afterPoint.length() >= 3){
            if (Character.getNumericValue(afterPoint.charAt(2)) < 5){
                return beforePoint+"."+afterPoint.substring(0,2);
            }
            else {
                int roundUp = Character.getNumericValue(afterPoint.charAt(1)) + 1;
                return beforePoint+"."+afterPoint.substring(0,1)+String.valueOf(roundUp);
            }
        }
        else {
            return number;
        }
    }

    public String convertSmallDecimalNum(String number){
        String [] numSplited = number.split("\\.");
        String beforePoint = numSplited[0];
        String afterPoint = numSplited[1];
        if (afterPoint.length() >= 6){
            if (Character.getNumericValue(afterPoint.charAt(5)) < 5){
                return beforePoint+"."+afterPoint.substring(0,5);
            }
            else {
                int roundUp = Character.getNumericValue(afterPoint.charAt(4)) + 1;
                return beforePoint+"."+afterPoint.substring(0,4)+String.valueOf(roundUp);
            }
        }
        else {
            return number;
        }
    }

    public void removeStopWords(){
        for (Iterator<Map.Entry<String, Term>> it = terms.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Term> entry = it.next();
            Term term = entry.getValue();
            if (this.upperCaseWords.containsKey(term.getTerm().toUpperCase())){
                upperCaseWords.remove(term.getTerm().toUpperCase());
                if (stopWords.contains(term.getTerm())){
                    it.remove();
                    stopWords.remove(term.getTerm());
                }
            }
        }
        this.terms.putAll(upperCaseWords);
    }

    public void addTerm(String stringTerm, Doc doc, Boolean toStem, int indexInText){
        Term term;
        if (terms.containsKey(stringTerm)){
            term = terms.get(stringTerm);
        }
        else {
            term = new Term(stringTerm);
        }
        if (doc == null){
            if (toStem){
                term = stemmer.stem(term);
            }
            terms.put(term.getTerm(), term);
        }
        else {
            int isInTitle = 0;
            if (doc.getTitle().contains(term.getTerm().toUpperCase()) || doc.getTitle().contains(term.getTerm().toLowerCase())){
                isInTitle = 1;
            }
            if (toStem){
                term = stemmer.stem(term);
            }
            if (terms.containsKey(term.getTerm()) && terms.get(term.getTerm()).getDocuments().containsKey(doc.getDocNumber())){
                terms.get(term.getTerm()).setExistingDoc(doc.getDocNumber(), indexInText);
                int currentTf = terms.get(term.getTerm()).getDocuments().get(doc.getDocNumber()).get(0);
                if (currentTf > doc.getMax_tf()){
                    doc.setMax_tf(currentTf);
                }
            }
            else {
                terms.put(term.getTerm(), term);
                terms.get(term.getTerm()).addNewDocument(doc.getDocNumber(), doc.getDocLength(), isInTitle, indexInText);
                int currentTf = terms.get(term.getTerm()).getDocuments().get(doc.getDocNumber()).get(0);
                if (currentTf > doc.getMax_tf()){
                    doc.setMax_tf(currentTf);
                }
                doc.setUniqueWordCount(doc.getUniqueWordCount()+1);
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

    private boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(,\\d+)*(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private boolean isFraction(String str){
        return str.matches("\\d+/\\d+|\\.\\d+");
    }

    private void fillMonthDic(){
        this.months = new HashMap<>();
        String[] monthArr = {"JANUARY", "january", "January", "jan", "JAN", "Jan", "FEBRUARY", "february", "February", "feb", "FEB", "Feb", "MARCH", "march", "March", "mar", "MAR", "Mar", "APRIL", "april", "April", "apr", "APR", "Apr", "MAY", "may", "May", "JUNE", "june", "June", "jun", "JUN", "Jun", "JULY", "july", "July", "jul", "JUL", "Jul", "AUGUST", "august", "August", "aug", "AUG", "Aug", "SEPTEMBER", "september", "September", "sep", "SEP", "Sep", "OCTOBER", "october", "October", "oct", "OCT", "Oct", "NOVEMBER", "november", "November", "nov", "NOV", "Nov", "DECEMBER", "december", "December", "dec", "DEC", "Dec"};
        String[] numsArr = {"01", "01", "01", "01", "01", "01", "02", "02", "02", "02", "02", "02", "03", "03", "03", "03", "03", "03", "04", "04", "04", "04", "04", "04", "05", "05", "05", "06", "06", "06", "06", "06", "06", "07", "07", "07", "07", "07", "07", "08", "08", "08", "08", "08", "08", "09", "09", "09", "09", "09", "09", "10", "10", "10", "10", "10", "10", "11", "11", "11", "11", "11", "11", "12", "12", "12", "12", "12", "12"};
        for (int i = 0; i < monthArr.length ; i++) {
            this.months.put(monthArr[i], numsArr[i]);
        }
    }

}
