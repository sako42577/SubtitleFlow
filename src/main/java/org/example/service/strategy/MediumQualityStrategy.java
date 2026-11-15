package org.example.service.strategy;

public class MediumQualityStrategy implements VideoQualityStrategy {
    @Override
    public String getQualityFlag() {
        return "-f \"bv*[height<=720]+ba/b[height<=720]\"";
    }
}