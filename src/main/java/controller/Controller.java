package controller;

import model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

/**
 * a controller class for connecting between view and model.
 */
public class Controller implements Runnable{

    private List<Long> result;
    private String corpusPath;
    private String stopWordsPath;
    private String dicPath;
    private boolean toStem;
    private UploadDictionary uploadDictionary;
    private Map<String, Doc> docsData;
    public Map<String, List<String>> docsDataNoStart;
    private Set<String> stopWords;

    public Controller(String stopWordsPath, String dicPath, boolean toStem){
        this.dicPath = dicPath;
        this.stopWordsPath = stopWordsPath;
        this.toStem = toStem;
        if (toStem){
            this.uploadDictionary = new UploadDictionary(dicPath+"citiesDictionaryFile", dicPath+"stemmedDictionaryFile");
        }
        else {
            this.uploadDictionary = new UploadDictionary(dicPath+"citiesDictionaryFile", dicPath+"dictionaryFile");
        }
        this.docsDataNoStart = new HashMap<>();
        this.stopWords = new HashSet<>();
    }

    public Controller(String corpusPath, String stopWordsPath, String dicPath, boolean toStem) {
        this.result = new ArrayList<>();
        this.corpusPath = corpusPath;
        this.stopWordsPath = stopWordsPath;
        this.dicPath = dicPath;
        this.toStem = toStem;
        this.docsData = new HashMap<>();
        if (toStem){
            this.uploadDictionary = new UploadDictionary(dicPath+"citiesDictionaryFile", dicPath+"stemmedDictionaryFile");
        }
        else {
            this.uploadDictionary = new UploadDictionary(dicPath+"citiesDictionaryFile", dicPath+"dictionaryFile");
        }
        this.docsDataNoStart = new HashMap<>();
        this.stopWords = new HashSet<>();
    }

    @Override
    public void run() {
        start();
    }

    public UploadDictionary getUploadDictionary() {
        return uploadDictionary;
    }

    public void fillStopWordsSet() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(stopWordsPath));
            String line;
            while ((line = br.readLine()) != null) {
                if (!(line.equals("b") || line.equals("c") || line.equals("d") || line.equals("e") || line.equals("f") || line.equals("g") || line.equals("h") || line.equals("i") || line.equals("j") || line.equals("k") || line.equals("l") || line.equals("m") || line.equals("n") || line.equals("o") || line.equals("p") || line.equals("q") || line.equals("r") || line.equals("s") || line.equals("t") || line.equals("u") || line.equals("v") || line.equals("w") || line.equals("x") || line.equals("y") || line.equals("z")))
                    stopWords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDicPath() {
        return dicPath;
    }

    /**
     * upload dictionary from given file path.
     */
    public void uploadDictionaries(){
        try {
            uploadDictionary.uploadCitiesDictionary();
            uploadDictionary.uploadDictionary();
        }
        catch (Exception e) {
        }

    }

    /**
     *
     * @return the result after running start method.
     */
    public List<Long> getResult() {
        return result;
    }

    /**
     * gets a city name and returns its details from the api.
     */
    private  List<String> updateCityDetails(String cityName) {
        List<String> details = new ArrayList<>();
        try {
            URL url = new URL("https://restcountries.eu/rest/v2/capital/" + cityName.toLowerCase());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            String jsonString = "";
            String line;
            while ((line = br.readLine()) != null) {
                jsonString = jsonString + line;
            }
            br.close();
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject obj = (JSONObject) jsonArray.get(0);
            details.add((String)obj.get("name"));
            String population =String.valueOf(obj.get("population"));
            details.add(convertPopulotianNum(population));
            JSONArray currency = obj.getJSONArray("currencies");
            JSONObject currencyObj = (JSONObject) currency.get(0);
            details.add((String)currencyObj.get("code"));
            //int capitalCounter = 1;
            //System.out.println(capitalCounter);
        } catch (Exception e) {
            if (e instanceof  FileNotFoundException){
                try {
                    URL url = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + cityName.toLowerCase());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    String geoString = "";
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        geoString += line;
                    }
                    br.close();
                    JSONObject obj = new JSONObject(geoString);
                    details.add((String)obj.get("geobytescountry"));
                    String population =String.valueOf(obj.get("geobytespopulation"));
                    details.add(convertPopulotianNum(population));
                    details.add((String)obj.get("geobytescurrencycode"));
                }
                catch (Exception e1){
                }
            }
        }
        return details;
    }

    /**
     * gets population string ang parse according to parse rules.
     */
    private String convertPopulotianNum(String popNum){
        Parse parse = new Parse();
        Term term = null;
        parse.parse(null, popNum, false);
        for (Iterator<Map.Entry<String, Term>> it = parse.getTerms().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Term> entry = it.next();
            term = entry.getValue();
        }
        return term.getTerm();
    }

    /**
     * starts indexing process - parse, indexer, merge and separate.
     */
    private void start() {
        long durationMS = 0;
        long start;
        start = System.currentTimeMillis();
        fillStopWordsSet();
        File folder = new File(corpusPath);
        File[] files = folder.listFiles();
        Map<String, Boolean> upperLowerDic = new HashMap<>();
        Map<String, List<String>> cityMap = new HashMap<>();
        Parse parse = new Parse(stopWords, upperLowerDic);
        int fileCounter = 1;
        int fileCounterIndex = 0;
        long docNum = 0;
        double doubleChunkSize = 40.0;
        int intChunkSize = 40;
        double indexSize = files.length / doubleChunkSize;
        String indexSizeString = String.valueOf(indexSize);
        String[] splitedDobule = indexSizeString.split("\\.");
        List<Thread> indexerThredsList = new ArrayList<>();
        boolean isRest = false;
        boolean lastIndexer = false;
        if (Integer.valueOf(splitedDobule[1]) > 0) {
            isRest = true;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                ReadFile readFile = new ReadFile(file.getAbsolutePath() + "\\" + file.getName());
                readFile.fillDocumentSet();
                for (Doc doc : readFile.getDocumentSet()) {
                    String cityName = doc.getCity();
                    if (!cityName.equals("") && !cityMap.containsKey(cityName)) {
                        List<String> cityData = updateCityDetails(cityName);
                        cityMap.put(cityName, cityData);
                    }
                    docNum++;
                    parse.parse(doc, null, toStem);
                    doc.setText("");
                    docsData.put(doc.getDocNumber(), doc);
                }
                if (fileCounterIndex == Integer.valueOf(splitedDobule[0]) && isRest) {
                    lastIndexer = true;
                }
                if (fileCounter == intChunkSize || lastIndexer && fileCounter == (files.length - 1) % intChunkSize) {
                    fileCounterIndex++;
                    parse.removeStopWords();
                    Indexer indexer = new Indexer(dicPath, fileCounterIndex, parse.getTerms());
                    Thread thread = new Thread(indexer);
                    thread.start();
                    indexerThredsList.add(thread);
                    parse = new Parse(stopWords, upperLowerDic);
                    fileCounter = 0;
                }
                fileCounter++;
            }
        }
        for (Thread t : indexerThredsList) {
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Merge merge = new Merge(dicPath, upperLowerDic, toStem, cityMap);
        merge.merge();
        File docsDataFile;
        if (toStem){
            docsDataFile = new File(dicPath+"stemmedDocsData");
        }
        else {
            docsDataFile = new File(dicPath+"docsData");
        }
        try {
            PrintWriter pw = new PrintWriter(docsDataFile);
            for (Iterator<Map.Entry<String, Doc>> it = docsData.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Doc> entry = it.next();
                Doc doc = entry.getValue();
                StringBuilder docLine = new StringBuilder();
                docLine.append(doc.getDocNumber());
                docLine.append("$");
                docLine.append(doc.getDocLength());
                docLine.append("$");
                if (doc.getCity().equals("") || doc.getCity() == null)
                    docLine.append("noCity");
                else
                    docLine.append(doc.getCity());
                docLine.append("$");
                if (doc.getLanguage().equals("") || doc.getLanguage() == null)
                    docLine.append("noLanguage");
                else
                    docLine.append(doc.getLanguage());
                docLine.append("$");
                for (String term: doc.getUpperTermsString()) {
                    docLine.append(term);
                    docLine.append("$");
                }
                pw.println(docLine);
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        long termsCounter = merge.separatePosting();
        merge.separateCitiesPosting();
        durationMS += System.currentTimeMillis() - start;
        Long runningTime = durationMS / 1000;
        result.add(docNum);
        result.add(termsCounter);
        result.add(runningTime);
    }

    /**
     * reset system - by deleting files and cleaning memory.
     */
    public void reset(){
        this.docsData = null;
        this.stopWords = null;
        this.docsDataNoStart = null;
        this.result = null;
        this.uploadDictionary.resetDictionaries();
        this.uploadDictionary = null;
        File folder = new File(this.dicPath);
        File [] files = folder.listFiles();
        for (File file: files) {
            file.delete();
        }
    }

    /**
     * This function returns a max entity map by a given document number.
     */
    public Map<String, Double> getMaxEntities(String docNumber) {
        Map<String, Double> unsortedResult = new LinkedHashMap<>();
        Map<String, Double> sortedResult;

        List<String> docData = this.docsDataNoStart.get(docNumber);
        for (int i = 3; i < docData.size() ; i++) {
            String entity = docData.get(i);
            if (Character.isDigit(entity.charAt(0))) {
                continue;
            }
            Map<String, List<Integer>> dictionary = uploadDictionary.getDictionary();
            if (dictionary.containsKey(entity.toUpperCase())) {
                int pointer = dictionary.get(entity.toUpperCase()).get(2);
                try {
                    String postingPath;
                    if (toStem) {
                        postingPath = dicPath+"stemmedPostingFile";
                    }
                    else {
                        postingPath = dicPath+"postingFile";
                    }
                    RandomAccessFile randomAccessFile = new RandomAccessFile(postingPath, "r");
                    randomAccessFile.seek(pointer);
                    String line = randomAccessFile.readLine();
                    String [] splitedLine = line.split("\\$");
                    for (int j = 0; j < splitedLine.length - 1; j += 2) {
                        if (docNumber.equals(splitedLine[j])) {
                            String [] splitedData = splitedLine[j+1].split(",");
                            double tf = Double.valueOf(splitedData[0]);
                            double docLength = Double.valueOf(this.docsDataNoStart.get(docNumber).get(0));
                            double entityRank = tf / docLength;
                            unsortedResult.put(entity.toUpperCase(), entityRank);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        sortedResult = unsortedResult.entrySet().stream().sorted(reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        sortedResult = getfirstFiveEntities(sortedResult);
        return sortedResult;
    }

    /**
     * This function gets all data from gui and runs a query.
     * @param cities - list of chosen cities, can be empty.
     * @param query - the query string.
     * @param toStem - true if stemming option was chosen, false if not.
     * @param doSemantic - true if the semantic option was chosen, false if not.
     * @param description - description if the query was given by a query file. description is empty if the query was a single query.
     * @param docLanguage - chosen doc language, can be empty.
     * @return - a map of 50 relevant documents and there rank.
     */
    public Map<String, Double> runQuery(List<String> cities, String query, boolean toStem, boolean doSemantic, String description, String docLanguage) {
        fillStopWordsSet();
        try {
            if (docsDataNoStart.size() == 0) {
                BufferedReader br;
                if (toStem)
                    br = new BufferedReader(new FileReader(this.dicPath + "stemmedDocsData"));
                else
                    br = new BufferedReader(new FileReader(this.dicPath + "docsData"));
                String line = br.readLine();
                while (line != null) {
                    String[] splitedLine = line.split("\\$");
                    String docNumber = splitedLine[0];
                    List<String> docList = new ArrayList<>();
                    for (int i = 1; i < splitedLine.length; i++) {
                        docList.add(splitedLine[i]);
                    }
                    docsDataNoStart.put(docNumber, docList);
                    line = br.readLine();
                }
                br.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Map<String, List<Integer>> dictionary = uploadDictionary.getDictionary();
        Map<String, List<String>> citiesDictionary = uploadDictionary.getCitiesDictionary();
        String postingPath;
        if (toStem)
            postingPath = this.dicPath+"stemmedPostingFile";
        else
            postingPath = this.dicPath+"postingFile";

        Searcher searcher = new Searcher(query, dictionary, citiesDictionary, postingPath, toStem,  cities, dicPath+"citiesPostingFile", docsDataNoStart, doSemantic, stopWords, description, docLanguage);
        searcher.queryHandle();
        return searcher.getResultForQuery();
    }

    /**
     * This function returns the five most important entities from the given entities map.
     */
    public Map<String, Double> getfirstFiveEntities(Map<String, Double> sortedEntities) {
        Map<String, Double> firstFive = new LinkedHashMap<>();
        int maxEntities = 5;
        int countEntities = 1;

        for (Map.Entry<String, Double> entry: sortedEntities.entrySet()){
            if (countEntities > maxEntities)
                break;
            firstFive.put(entry.getKey(), entry.getValue());
            countEntities++;

        }
        return firstFive;
    }
}
