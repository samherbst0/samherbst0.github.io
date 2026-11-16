package com.samherbst.weighttracker;

public class WeightEntry {
    private int id;
    private String date;
    private double weight;

    public WeightEntry(int id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
