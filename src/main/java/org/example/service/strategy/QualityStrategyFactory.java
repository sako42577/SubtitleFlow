package org.example.service.strategy;

public class QualityStrategyFactory {
    public static VideoQualityStrategy getStrategy(String quality) {
        if (quality == null) {
            return new MediumQualityStrategy();
        }
        switch (quality) {
            case "360p":
                return new LowQualityStrategy();
            case "720p":
                return new MediumQualityStrategy();
            case "best":
            default:
                return new BestQualityStrategy();
        }
    }
}