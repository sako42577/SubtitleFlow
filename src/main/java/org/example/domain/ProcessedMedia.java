package org.example.domain;

public class ProcessedMedia {
    private String finalFilePath;
    private String format;

    public ProcessedMedia(String finalFilePath) {
        this.finalFilePath = finalFilePath;
        this.format = "mp4";
    }

    public String getFinalFilePath() {
        return finalFilePath;
    }

    public void setFinalFilePath(String finalFilePath) {
        this.finalFilePath = finalFilePath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}