package org.example.service.downloader;

public interface VideoDownloader {
    // Теперь интерфейс требует язык, чтобы знать, какие субтитры качать
    String download(String url, String videoId, String lang);
}