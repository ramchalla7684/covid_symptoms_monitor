package com.nebuxe.cse535assignment.pojos;

import java.util.HashMap;

public class HealthRecord {
    private long date;

    private int heartBeatRate;
    private int respiratoryRate;

    private HashMap<String, Integer> symptomRatings;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getHeartBeatRate() {
        return heartBeatRate;
    }

    public void setHeartBeatRate(int heartBeatRate) {
        this.heartBeatRate = heartBeatRate;
    }

    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(int respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public HashMap<String, Integer> getSymptomRatings() {
        return symptomRatings;
    }

    public void setSymptomRatings(HashMap<String, Integer> symptomRatings) {
        this.symptomRatings = symptomRatings;
    }
}
