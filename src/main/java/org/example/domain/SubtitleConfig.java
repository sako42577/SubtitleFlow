package org.example.domain;

public class SubtitleConfig {
    private String sourceLanguage;
    private String targetLanguage;
    private String fontColor;

    public SubtitleConfig() {}

    private SubtitleConfig(Builder builder) {
        this.sourceLanguage = builder.sourceLanguage;
        this.targetLanguage = builder.targetLanguage;
        this.fontColor = builder.fontColor;
    }

    public String getSourceLanguage() { return sourceLanguage; }
    public String getTargetLanguage() { return targetLanguage; }
    public String getFontColor() { return fontColor; }

    public static class Builder {
        private String sourceLanguage = "auto";
        private String targetLanguage = "en";
        private String fontColor = "white";

        public Builder setSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
            return this;
        }

        public Builder setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
            return this;
        }

        public Builder setFontColor(String fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public SubtitleConfig build() {
            return new SubtitleConfig(this);
        }
    }
}