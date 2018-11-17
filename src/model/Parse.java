package model;

import java.util.*;

public class Parse {
    private List<String> tokens;
    private Set<Term> terms;
    private Set<String> stopWords;
    private Map<String, String> months;

    public Parse(Set<String> stopWords) {
        this.terms = new HashSet<>();
        this.stopWords = stopWords;
        fillMonthDic();
    }

    public void parse(String text){
        this.tokens = new ArrayList<>(Arrays.asList(text.split("\\s+")));

        for (int i = 0; i <tokens.size() ; i++) {
            String token = tokens.get(i);

            if (token.length() <= 1){
                continue;
            }

            if (token.charAt(token.length()-1) == ',' || token.charAt(token.length()-1) == '.' || token.charAt(token.length()-1) == ';' ||token.charAt(token.length()-1) == '?' || token.charAt(token.length()-1) == '!' || token.charAt(token.length()-1) == '-' || token.charAt(token.length()-1) == ':' || token.charAt(token.length()-1) == '"' || token.charAt(token.length()-1) == '}' || token.charAt(token.length()-1) == ')'){
                token = token.substring(0,token.length()-1);
            }

            if (token.charAt(0) == ',' || token.charAt(0) == '.' || token.charAt(0) == ';' || token.charAt(0) == '?' || token.charAt(0) == '!' || token.charAt(0) == '-' || token.charAt(0) == ':' || token.charAt(0) == '"' || token.charAt(0) == '{' || token.charAt(0) == '('){
                token = token.substring(1);
            }

            if (token.length() <= 1){
                continue;
            }
            /**
            // upper and lower case letters.
            if (Character.isUpperCase(token.charAt(0))){
                if (!tokens.contains(token.toLowerCase())){
                    terms.add(new Term(token.toUpperCase()));
                    continue;
                }
                else {
                    terms.add(new Term(token.toLowerCase()));
                    continue;
                }
            }
             **/
            // removing stop words.
            if (stopWords.contains(token)){
                terms.remove(new Term(token));
                tokens.remove(i);
                continue;
            }

            // dealing with percentage.
            if (token.equals("percent") || token.equals("percentage")){
                if (i-1 >=0 && isNumeric(tokens.get(i-1))){
                    terms.add(new Term(tokens.get(i-1)+"%"));
                }
            }

            // prices
            else if (token.equals("Dollars")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    Double price = Double.parseDouble(tokens.get(i-1).replace(",",""));
                    if (price < 1000000){
                        terms.add(new Term(tokens.get(i-1)+" Dollars"));
                    }
                    else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")){
                            priceStr = priceStr.replace(".0", "");
                        }
                        terms.add(new Term(priceStr+" M"+" Dollars"));
                    }
                }
                else {
                    if (i-1 >= 0 && isFraction(tokens.get(i-1))){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            Double price = Double.parseDouble(tokens.get(i-2).replace(",",""));
                            if (price < 1000000){
                                terms.add(new Term(tokens.get(i-2)+" "+tokens.get(i-1)+" Dollars"));
                            }
                        }
                    }
                    if (i-1 >=0 && tokens.get(i-1).equals("m")){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            terms.add(new Term(tokens.get(i-2)+" M"+" Dollars"));
                        }
                    }
                    if (i-1 >=0 && tokens.get(i-1).equals("bn")){
                        if (i-2 >= 0 && isNumeric(tokens.get(i-2))){
                            terms.add(new Term(tokens.get(i-2)+"000 M"+" Dollars"));
                        }
                    }
                }
            }
            else if (token.contains("$")){
                String newToken = token.replace("$","");
                if (i+1 < tokens.size() && tokens.get(i+1).equals("million")){
                    terms.add(new Term(newToken+" M"+" Dollars"));
                }
                if (i+1 < tokens.size() && tokens.get(i+1).equals("billion")){
                    terms.add(new Term(newToken+"000 M"+" Dollars"));
                }
                else if (isNumeric(newToken)){
                    Double price = Double.parseDouble(newToken.replace(",",""));
                    if (price < 1000000){
                        terms.add(new Term(newToken+" Dollars"));
                    }
                    else {
                        price = price / 1000000;
                        String priceStr = price.toString();
                        if (priceStr.endsWith(".0")){
                            priceStr = priceStr.replace(".0", "");
                        }
                        terms.add(new Term(priceStr+" M"+" Dollars"));
                    }
                }
            }

            else if (token.equals("dollars")){
                if (i-1 >=0 && tokens.get(i-1).equals("U.S.")){
                    if (i-2 >=0 && tokens.get(i-2).equals("million")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            terms.add(new Term(tokens.get(i-3)+" M"+" Dollars"));
                        }
                    }
                    else if (i-2 >=0 && tokens.get(i-2).equals("billion")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            terms.add(new Term(tokens.get(i-3)+"000 M"+" Dollars"));
                        }
                    }
                    else if (i-2 >=0 && tokens.get(i-2).equals("trillion")){
                        if (i-3 >=0 && isNumeric(tokens.get(i-3))){
                            terms.add(new Term(tokens.get(i-3)+"000000 M"+" Dollars"));
                        }
                    }
                }
            }
            // dates
            else if (months.containsKey(token)){
                if (i-1 >= 0 && tokens.get(i-1).matches("[0-9]+")){
                    int date = Integer.valueOf(tokens.get(i-1));
                    if (date <= 31){
                        terms.add(new Term(months.get(token)+"-"+date));
                    }
                }
                if (i+1 < tokens.size() && tokens.get(i+1).matches("[0-9]+")){
                    int date = Integer.parseInt(tokens.get(i+1));
                    if (date <= 31){
                        if (date < 10){
                            terms.add(new Term(months.get(token)+"-0"+date));
                        }
                        else {
                            terms.add(new Term(months.get(token)+"-"+date));
                        }
                    }
                    else {
                        terms.add(new Term(date+"-"+months.get(token)));
                    }
                }
            }
            else if (token.equals("between") || token.equals("Between")){
                if (i+1 < tokens.size() && isNumeric(tokens.get(i+1))){
                    if (i+2 < tokens.size() && tokens.get(i+2).equals("and")){
                        if (i+3 < tokens.size() && isNumeric(tokens.get(i+3))){
                            terms.add(new Term(token+" "+tokens.get(i+1)+" "+tokens.get(i+2)+" "+tokens.get(i+3)));
                        }
                    }
                }
            }
            // Numbers
            else if (token.equals("Thousand")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    terms.add(new Term(tokens.get(i-1)+"K"));
                }
            }
            else if (token.equals("Million")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    terms.add(new Term(tokens.get(i-1)+"M"));
                }
            }
            else if (token.equals("Billion")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    terms.add(new Term(tokens.get(i-1)+"B"));
                }
            }
            else if (token.equals("Trillion")){
                if (i-1 >= 0 && isNumeric(tokens.get(i-1))){
                    terms.add(new Term(tokens.get(i-1)+"00B"));
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
                    terms.add(new Term(tokenStr+"K"));
                }
                else if (newToken >= 1000000 && newToken < 1000000000){
                    newToken = newToken / 1000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    terms.add(new Term(tokenStr+"M"));
                }
                else if (newToken >= 1000000000){
                    newToken = newToken / 1000000000;
                    String tokenStr = newToken.toString();
                    if (tokenStr.endsWith(".0")){
                        tokenStr = tokenStr.replace(".0", "");
                    }
                    terms.add(new Term(tokenStr+"B"));
                }
                else if (newToken < 1000){
                    if (i+1 < tokens.size() && !(tokens.get(i+1).equals("Million") || tokens.get(i+1).equals("Billion") || tokens.get(i+1).equals("Trillion") || tokens.get(i+1).equals("Thousand"))){
                        terms.add(new Term(token));
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
                        terms.add(new Term(tokens.get(i-1)+" "+token));
                    }
                }
            }
            // Evrey thing else
            else {
                terms.add(new Term(token));
            }
        }
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
        return str.matches("-?\\d+(\\,\\d+)*(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private boolean isFraction(String str){
        return str.matches("\\d+\\/\\d+|\\.\\d+");
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
