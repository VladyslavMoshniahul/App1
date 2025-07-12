package com.example.demo.javaSrc.voting;



import java.util.LinkedHashMap;
import java.util.Map;

public class VotingResults {
    private String title;
    private String description;
    private boolean multipleChoice;
    private Map<String, Long> variantCounts;
    private long totalVotes;

    public VotingResults(String title, String description, boolean multipleChoice) {
        this.title = title;
        this.description = description;
        this.multipleChoice = multipleChoice;
        this.variantCounts = new LinkedHashMap<>();
        this.totalVotes = 0;
    }

    public void addVariantResult(String variantText, long count) {
        this.variantCounts.put(variantText, count);
        this.totalVotes += count;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isMultipleChoice() { return multipleChoice; }
    public Map<String, Long> getVariantCounts() { return variantCounts; }
    public long getTotalVotes() { return totalVotes; }
}