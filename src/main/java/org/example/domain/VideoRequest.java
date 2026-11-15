package org.example.domain;

import org.example.domain.state.DownloadingState;
import org.example.domain.state.VideoState;

public class VideoRequest {
    private String id;
    private String originalUrl;
    private VideoState state;

    // Черновик текста субтитров
    private Transcript draftTranscript;

    // НОВОЕ ПОЛЕ: Настройки (язык и цвет), чтобы сервер помнил их при рендере
    private SubtitleConfig config;

    // Пустой конструктор для библиотеки Jackson
    public VideoRequest() {
    }

    public VideoRequest(String id, String originalUrl) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.state = new DownloadingState();
    }

    // --- НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ НАСТРОЕК ---
    public SubtitleConfig getConfig() {
        return config;
    }

    public void setConfig(SubtitleConfig config) {
        this.config = config;
    }

    // --- ОСТАЛЬНЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ---
    public Transcript getDraftTranscript() {
        return draftTranscript;
    }

    public void setDraftTranscript(Transcript draftTranscript) {
        this.draftTranscript = draftTranscript;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public void setState(VideoState state) { this.state = state; }

    public String getCurrentState() {
        if (state == null) return "UNKNOWN";
        return state.getStatus();
    }

    public void nextState() {
        if (state != null) state.next(this);
    }
}