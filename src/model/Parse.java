package model;

import java.util.*;

public class Parse {
    private List<String> tokens;
    private Set<Term> terms;
    private Set<String> stopWords;
    private Map<String, String> months;
    private Set<Term> upperCaseWords;
    private Stemmer stemmer;


    public Parse(Set<String> stopWords) {
        this.terms = new HashSet<>();
        this.stopWords = stopWords;
        this.upperCaseWords = new HashSet<>();
        this.stemmer = new Stemmer();
        fillMonthDic();
    }

    public void parse(Doc doc, Boolean toStem){
        String text = doc.getText();
        boolean toChangeLine = false;
        int line = 0;
        int indexInLine = 0;
        this.tokens = new ArrayList<>(Arrays.asList(text.split("([^0-9a-zA-Z%]+)?\\s+([^$0-9a-zA-Z]+)?")));
        System.out.println(tokens.toString());
        for (int i = 0; i <tokens.size() ; i++) {
            String token = tokens.get(i);

            if (token.contains("\n")){
                token = token.replaceAll("\\n","");
                toChangeLine = true;
            }

            if (token.length() <= 1){
                continue;
            }

            // upper and lower case letters.
            if (Character.isUpperCase(token.charAt(0))){
                this.upperCaseWords.add(new Term(token.toUpperCase()));
            }

            // dealing with percentage.
            if (token.equals("percent") || token.equals("percentage")){
                if (i-1 >=0 && isNumeric(tokens.get(i-1))){
                    Term term = new Term(tokens.get(i-1)+"%");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
            }

            // prices
            else if (token.equals("Dollars")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Double price = Double.parseDouble(tokens.get(i-1).replace(",",""));
                    if (price < 1000000){
                        Term term = new Term(tokens.get(i-1)+" Dollars");
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                    else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")){
                            priceStr = priceStr.replace(".0", "");
                        }
                        Term term = new Term(priceStr+" M"+" Dollars");
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                }
                else {
                    if (i-1 >= 0 && isFraction(tokens.get(i-1))){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            Double price = Double.parseDouble(tokens.get(i-2).replace(",",""));
                            if (price < 1000000){
                                Term term = new Term(tokens.get(i-2)+" "+tokens.get(i-1)+" Dollars");
                                addTerm(term, doc, toStem, line, indexInLine);
                            }
                        }
                    }
                    if (i-1 >=0 && tokens.get(i-1).equals("m")){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            Term term = new Term(tokens.get(i-2)+" M"+" Dollars");
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                    if (i-1 >=0 && tokens.get(i-1).equals("bn")){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            Term term = new Term(tokens.get(i-2)+"000 M"+" Dollars");
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                }
            }
            else if (token.contains("$")){
                String newToken = token.replace("$","");
                if (i+1 < tokens.size() && tokens.get(i+1).equals("million")){
                    Term term = new Term(newToken+" M"+" Dollars");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
                if (i+1 < tokens.size() && tokens.get(i+1).equals("billion")){
                    Term term = new Term(newToken+"000 M"+" Dollars");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
                else if (isNumeric(newToken)){
                    Double price = Double.parseDouble(newToken.replace(",",""));
                    if (price < 1000000){
                        Term term = new Term(newToken+" Dollars");
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                    else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")){
                            priceStr = priceStr.replace(".0", "");
                        }
                        Term term = new Term(priceStr+" M"+" Dollars");
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                }
            }

            else if (token.equals("dollars")){
                if (i-1 >=0 && tokens.get(i-1).equals("U.S.")){
                    if (i-2 >=0 && tokens.get(i-2).equals("million")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            Term term = new Term(tokens.get(i-3)+" M"+" Dollars");
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                    else if (i-2 >=0 && tokens.get(i-2).equals("billion")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            Term term = new Term(tokens.get(i-3)+"000 M"+" Dollars");
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                    else if (i-2 >=0 && tokens.get(i-2).equals("trillion")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            Term term = new Term(tokens.get(i-3)+"000000 M"+" Dollars");
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                }
            }
            // dates
            else if (months.containsKey(token)){
                if (i-1 >= 0 && tokens.get(i-1).matches("[0-9]+")){
                    int date = Integer.valueOf(tokens.get(i-1));
                    if (date <= 31){
                        Term term = new Term(months.get(token)+"-"+date);
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                }
                if (i+1 < tokens.size() && tokens.get(i+1).matches("[0-9]+")){
                    int date = Integer.parseInt(tokens.get(i+1));
                    if (date <= 31){
                        if (date < 10){
                            Term term = new Term(months.get(token)+"-0"+date);
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                        else {
                            Term term = new Term(months.get(token)+"-"+date);
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                    else {
                        Term term = new Term(date+"-"+months.get(token));
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                }
            }
            else if (token.equals("between") || token.equals("Between")){
                if (i+1 < tokens.size() && isNumeric(tokens.get(i+1))){
                    if (i+2 < tokens.size() && tokens.get(i+2).equals("and")){
                        if (i+3 < tokens.size() && isNumeric(tokens.get(i+3))){
                            Term term = new Term(token+" "+tokens.get(i+1)+" "+tokens.get(i+2)+" "+tokens.get(i+3));
                            addTerm(term, doc, toStem, line, indexInLine);
                        }
                    }
                }
            }
            // Numbers
            else if (token.equals("Thousand")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Term term = new Term(tokens.get(i-1)+"K");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
            }
            else if (token.equals("Million")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Term term = new Term(tokens.get(i-1)+"M");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
            }
            else if (token.equals("Billion")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Term term = new Term(tokens.get(i-1)+"B");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
            }
            else if (token.equals("Trillion")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Term term = new Term(tokens.get(i-1)+"00B");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
            }
            else if (isNumeric(token)){
                Double newToken = Double.parseDouble(token.replace(",",""));
                if (newToken >= 1000 && newToken < 1000000){
                    newToken = newToken / 1000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    Term term = new Term(tokenStr+"K");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
                else if (newToken >= 1000000 && newToken < 1000000000){
                    newToken = newToken / 1000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    Term term = new Term(tokenStr+"M");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
                else if (newToken >= 1000000000){
                    newToken = newToken / 1000000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    Term term = new Term(tokenStr+"B");
                    addTerm(term, doc, toStem, line, indexInLine);
                }
                else {
                    if (i+1 < tokens.size() && !(tokens.get(i+1).equals("Million") || tokens.get(i+1).equals("Billion") || tokens.get(i+1).equals("Trillion") || tokens.get(i+1).equals("Thousand"))){
                        Term term = new Term(token);
                        addTerm(term, doc, toStem, line, indexInLine);
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
                        Term term = new Term(tokens.get(i-1)+" "+token);
                        addTerm(term, doc, toStem, line, indexInLine);
                    }
                }
            }
            // Evrey thing else
            else {
                Term term = new Term(token.toLowerCase());
                addTerm(term, doc, toStem, line, indexInLine);
            }

            if (toChangeLine){
                line++;
                indexInLine = 0;
                toChangeLine = false;
            }
        }
    }

    public void removeStopWords(){
        for (Iterator<Term> it = terms.iterator(); it.hasNext(); ) {
            Term term = it.next();
            if (this.upperCaseWords.contains(new Term(term.getTerm().toUpperCase()))){
                upperCaseWords.remove(new Term(term.getTerm().toUpperCase()));
                if (stopWords.contains(term.getTerm())){
                    it.remove();
                    stopWords.remove(term.getTerm());
                }
            }
        }
        this.terms.addAll(upperCaseWords);
    }

    public void addTerm(Term term, Doc doc, Boolean toStem, int line, int indexInLine){
        int isInTitle = 0;
        if (doc.getTitle().contains(term.getTerm())){
            isInTitle = 1;
        }
        if (toStem){
            term = stemmer.stem(term);
        }
        if (doc.getTerms().containsKey(term)){
            doc.setTerms(term, doc.getTerms().get(term).get(0) + 1, line, indexInLine, isInTitle);
        }
        else {
            doc.setTerms(term, 1, line, indexInLine, isInTitle);
        }
        terms.add(term);
    }

    public Set<Term> getTerms() {
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
