package org.example.service;

import org.example.domain.SubtitleConfig;
import org.example.domain.Transcript;
import org.example.domain.VideoRequest;
import org.example.service.adapter.ConsoleCommandAdapter;
import org.example.service.downloader.YoutubeDownloader;
import org.example.service.observer.NotificationManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class VideoProcessingFacade {

    private NotificationManager notificationManager = new NotificationManager();
    private ConsoleCommandAdapter adapter = new ConsoleCommandAdapter();

    public NotificationManager getNotificationManager() { return notificationManager; }

    public VideoRequest startProcessing(String url, String sourceLang, String color) {
        VideoRequest request = createRequest(url, sourceLang, color);
        String downloadedId = downloadAudio(request);
        File outputDir = getOutputDirectory();

        try {
            File audioFile = findAudioFile(outputDir, downloadedId);
            generateSubtitles(audioFile, outputDir, sourceLang);
            request.setDraftTranscript(readTranscript(audioFile, outputDir));
        } catch (Exception e) {
            System.out.println("Facade Error: " + e.getMessage());
            request.setDraftTranscript(new Transcript("1\n00:00:00,000 --> 00:00:05,000\nError during processing."));
        }

        request.nextState();
        notificationManager.notifyAllSubscribers(request);

        return request;
    }

    private VideoRequest createRequest(String url, String sourceLang, String color) {
        String uniqueId = UUID.randomUUID().toString();
        VideoRequest request = new VideoRequest(uniqueId, url);

        SubtitleConfig config = new SubtitleConfig.Builder()
                .setSourceLanguage(sourceLang)
                .setFontColor(color)
                .build();
        request.setConfig(config);

        return request;
    }

    private String downloadAudio(VideoRequest request) {
        YoutubeDownloader downloader = new YoutubeDownloader();
        System.out.println("Facade: Downloading audio only for fast analysis...");
        return downloader.downloadAudioOnly(request.getOriginalUrl(), request.getId());
    }

    private File getOutputDirectory() {
        String projectDir = new File("").getAbsolutePath();
        return new File(projectDir + File.separator + "output");
    }

    private File findAudioFile(File outputDir, String downloadedId) throws IOException {
        // Ищем скачанный аудиофайл (.mp3)
        File[] audioFiles = outputDir.listFiles((d, name) ->
                name.startsWith(downloadedId)
                        && (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".webm"))
        );

        if (audioFiles == null || audioFiles.length == 0) {
            throw new IOException("Audio file not found in output folder.");
        }

        return audioFiles[0];
    }

    private void generateSubtitles(File audioFile, File outputDir, String sourceLang) {
        // Запускаем Whisper по аудиофайлу (это работает очень быстро)
        System.out.println("Facade: Starting OpenAI Whisper AI on audio...");
        String langParam = (sourceLang != null && !sourceLang.equals("auto"))
                ? "--language " + sourceLang
                : "";

        String whisperCommand = "/home/sanzhar/.local/bin/whisper \"" + audioFile.getAbsolutePath()
                + "\" --model tiny " + langParam
                + " --output_format srt --output_dir \"" + outputDir.getAbsolutePath() + "\"";

        adapter.executeCommand(whisperCommand);
    }

    private Transcript readTranscript(File audioFile, File outputDir) throws IOException {
        // Читаем .srt
        String baseName = audioFile.getName().substring(0, audioFile.getName().lastIndexOf('.'));
        File generatedSrt = new File(outputDir.getAbsolutePath() + File.separator + baseName + ".srt");

        String rawSubtitleText;
        if (generatedSrt.exists()) {
            rawSubtitleText = Files.readString(generatedSrt.toPath());
        } else {
            rawSubtitleText = "1\n00:00:00,000 --> 00:00:05,000\n[Ошибка: Whisper не нашел файл]";
        }

        return new Transcript(rawSubtitleText);
    }
}
