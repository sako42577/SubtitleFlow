package org.example.service.strategy;

public class BestQualityStrategy implements VideoQualityStrategy {
    @Override
    public String getQualityFlag() {
        return ""; // Без ограничений, скачивает оригинал
    }
}