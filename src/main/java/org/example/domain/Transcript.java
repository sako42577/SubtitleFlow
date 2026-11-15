package org.example.domain;

public class Transcript {
    private String rawText;
    private boolean isApprovedByUser;

    // ДОБАВЛЕН ПУСТОЙ КОНСТРУКТОР ДЛЯ БИБЛИОТЕКИ JACKSON
    public Transcript() {
    }

    public Transcript(String rawText) {
        this.rawText = rawText;
        this.isApprovedByUser = false;
    }

    public String getRawText() {
        return rawText;
    }

    // ДОБАВЛЕН СЕТТЕР ДЛЯ БИБЛИОТЕКИ JACKSON
    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public boolean isApprovedByUser() {
        return isApprovedByUser;
    }

    public void setApprovedByUser(boolean approvedByUser) {
        isApprovedByUser = approvedByUser;
    }
}