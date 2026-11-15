package org.example.service.strategy;

public class LowQualityStrategy implements VideoQualityStrategy {
    @Override
    public String getQualityFlag() {
        return "-f \"bv*[height<=360]+ba/b[height<=360]\"";
    }
}