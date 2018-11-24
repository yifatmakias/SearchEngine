package model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Merge {

    private String postingPath;
    private int postingFilesNumber;
    private ExecutorService poolExecutor;

    public Merge(String postingPath, int postingFilesNumber) {
        this.postingPath = postingPath;
        this.postingFilesNumber = postingFilesNumber;
        this.poolExecutor = Executors.newFixedThreadPool(postingFilesNumber);

    }



}
