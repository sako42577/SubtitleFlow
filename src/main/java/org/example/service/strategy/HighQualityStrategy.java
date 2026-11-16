package org.example.service.strategy;

public class HighQualityStrategy implements VideoQualityStrategy {
    @Override
    public String getQualityFlag() {
        return "-f \"bv*[height<=1080]+ba/b[height<=1080]\"";
    }
}
