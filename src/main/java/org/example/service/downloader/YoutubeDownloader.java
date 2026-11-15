package org.example.service.downloader;

import org.example.service.adapter.ConsoleCommandAdapter;
import org.example.service.strategy.QualityStrategyFactory;
import org.example.service.strategy.VideoQualityStrategy;

import java.io.File;

public class YoutubeDownloader implements VideoDownloader {
    private ConsoleCommandAdapter adapter = new ConsoleCommandAdapter();

    @Override
    public String download(String url, String videoId, String lang) {
        // Fallback method
        return downloadAudioOnly(url, videoId);
    }

    /**
     * 1. Fast audio extraction for Whisper AI analysis (downloads only mp3, very fast)
     */
    public String downloadAudioOnly(String url, String videoId) {
        String projectDir = new File("").getAbsolutePath();
        File outputDir = new File(projectDir + File.separator + "output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String outputTemplate = outputDir.getAbsolutePath() + File.separator + videoId + ".%(ext)s";

        // -x --audio-format mp3 extracts only audio stream
        String command = "yt-dlp -x --audio-format mp3 --user-agent \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36\" --no-check-certificate --output \"" + outputTemplate + "\" '" + url + "'";

        System.out.println("Executing fast audio downloader: " + command);
        adapter.executeCommand(command);

        return videoId;
    }

    /**
     * 2. Downloads video with quality based on the Strategy Pattern
     */
    public String downloadVideoWithQuality(String url, String videoId, String quality) {
        String projectDir = new File("").getAbsolutePath();
        File outputDir = new File(projectDir + File.separator + "output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String outputTemplate = outputDir.getAbsolutePath() + File.separator + videoId + ".%(ext)s";

        // DESIGN PATTERN: STRATEGY
        // Dynamically obtain the strategy object to determine the command-line quality flags
        VideoQualityStrategy strategy = QualityStrategyFactory.getStrategy(quality);
        String qualityFlag = strategy.getQualityFlag();

        String command = "yt-dlp " + qualityFlag + " --user-agent \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36\" --no-check-certificate --output \"" + outputTemplate + "\" '" + url + "'";

        System.out.println("Executing Strategy [Quality: " + quality + "] downloader: " + command);
        adapter.executeCommand(command);

        return videoId;
    }
}