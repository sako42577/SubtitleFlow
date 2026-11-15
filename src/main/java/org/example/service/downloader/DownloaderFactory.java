package org.example.service.downloader;

public class DownloaderFactory {

    public static VideoDownloader getDownloader(String url) {
        // Улучшенная логика: Фабрика понимает и длинные, и короткие ссылки ютуба
        if (url != null && (url.toLowerCase().contains("youtube.com") || url.toLowerCase().contains("youtu.be"))) {
            return new YoutubeDownloader();
        } else {
            return new GenericDownloader();
        }
    }
}