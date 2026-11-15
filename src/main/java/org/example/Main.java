package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.example.domain.ProcessedMedia;
import org.example.domain.SubtitleConfig;
import org.example.domain.Transcript;
import org.example.domain.VideoRequest;
import org.example.service.adapter.ConsoleCommandAdapter;
import org.example.service.downloader.YoutubeDownloader;
import org.example.service.observer.WebClientMock;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final Map<String, VideoRequest> database = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        WebClientMock myBrowser = new WebClientMock("Chrome_User_1");
        ConsoleCommandAdapter adapter = new ConsoleCommandAdapter();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(8080);

        // 1. Process URL + Settings: Download video & run Whisper with auto-detection
        app.post("/api/process", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String url = body.get("originalUrl");
            String quality = body.getOrDefault("videoQuality", "720p");
            String fontSize = body.getOrDefault("fontSize", "24");
            String color = body.getOrDefault("fontColor", "white");

            String uniqueId = UUID.randomUUID().toString();
            VideoRequest request = new VideoRequest(uniqueId, url);

            SubtitleConfig config = new SubtitleConfig.Builder()
                    .setFontColor(color)
                    .build();
            request.setConfig(config);

            String projectDir = new File("").getAbsolutePath();
            File outputDir = new File(projectDir + File.separator + "output");
            if (!outputDir.exists()) outputDir.mkdirs();

            try {
                // Скачиваем видео в выбранном качестве (Strategy Pattern)
                YoutubeDownloader downloader = new YoutubeDownloader();
                System.out.println("Downloading video with quality strategy: " + quality);
                downloader.downloadVideoWithQuality(url, uniqueId, quality);

                File[] videoFiles = outputDir.listFiles((d, name) ->
                        name.startsWith(uniqueId) &&
                                !name.endsWith(".srt") &&
                                !name.endsWith(".vtt") &&
                                !name.endsWith(".mp3") &&
                                !name.contains("_final")
                );

                if (videoFiles == null || videoFiles.length == 0) {
                    throw new Exception("Downloaded video file not found.");
                }
                String videoFilePath = videoFiles[0].getAbsolutePath();

                // Запускаем Whisper без принудительного языка (AI сам определит язык речи)
                System.out.println("Running Whisper AI with auto language detection...");
                String whisperCommand = "/home/sanzhar/.local/bin/whisper \"" + videoFilePath + "\" --model tiny --output_format srt --output_dir \"" + outputDir.getAbsolutePath() + "\"";

                adapter.executeCommand(whisperCommand);

                String baseName = videoFiles[0].getName().substring(0, videoFiles[0].getName().lastIndexOf('.'));
                File generatedSrt = new File(outputDir.getAbsolutePath() + File.separator + baseName + ".srt");

                String rawText = "";
                if (generatedSrt.exists()) {
                    rawText = Files.readString(generatedSrt.toPath());
                } else {
                    rawText = "1\n00:00:00,000 --> 00:00:05,000\n[Transcription warning: No subtitles generated]";
                }

                request.setDraftTranscript(new Transcript(rawText));
                database.put(uniqueId, request);

                ctx.json(request);

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error during processing: " + e.getMessage());
            }
        });

        // 2. Approve Subtitles & Render via FFmpeg (with custom Font Size & Color)
        app.post("/api/approve", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String rawText = (String) body.get("rawText");
            String fontSize = (String) body.getOrDefault("fontSize", "24");
            String videoId = ctx.queryParam("id");

            VideoRequest request = database.get(videoId);
            if (request == null) {
                ctx.status(404).result("Error: Video not found in database");
                return;
            }

            String projectDir = new File("").getAbsolutePath();
            File outputDir = new File(projectDir + File.separator + "output");

            String subFilePath = outputDir.getAbsolutePath() + File.separator + videoId + ".srt";
            String finalVideoPath = outputDir.getAbsolutePath() + File.separator + videoId + "_final.mp4";

            try {
                File[] originalFiles = outputDir.listFiles((d, name) ->
                        name.startsWith(videoId) &&
                                !name.endsWith(".srt") &&
                                !name.endsWith(".vtt") &&
                                !name.endsWith(".mp3") &&
                                !name.contains("_final")
                );

                if (originalFiles == null || originalFiles.length == 0) {
                    throw new Exception("Original video file not found!");
                }
                String originalVideoPath = originalFiles[0].getAbsolutePath();

                FileWriter writer = new FileWriter(subFilePath);
                writer.write(rawText);
                writer.close();

                // Цвет субтитров
                String colorCode = "&H00FFFFFF"; // White default
                if (request.getConfig() != null) {
                    String chosenColor = request.getConfig().getFontColor();
                    if ("yellow".equals(chosenColor)) colorCode = "&H0000FFFF";
                    if ("green".equals(chosenColor)) colorCode = "&H0000FF00";
                    if ("red".equals(chosenColor)) colorCode = "&H000000FF";
                }

                // FFmpeg вшивает субтитры с динамическим размером шрифта (FontSize)
                String ffmpegCommand = "ffmpeg -y -i \"" + originalVideoPath + "\" -vf \"subtitles=" + subFilePath + ":force_style='PrimaryColour=" + colorCode + ",FontSize=" + fontSize + "'\" -c:v libx264 -c:a aac \"" + finalVideoPath + "\"";

                System.out.println("Running FFmpeg burn-in with Font Size: " + fontSize);
                adapter.executeCommand(ffmpegCommand);

                ProcessedMedia finalMedia = new ProcessedMedia(finalVideoPath);
                ctx.json(finalMedia);

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Error during rendering: " + e.getMessage());
            }
        });

        // 3. Browser File Download Endpoint
        app.get("/api/download/{id}", ctx -> {
            String videoId = ctx.pathParam("id");
            String projectDir = new File("").getAbsolutePath();
            File file = new File(projectDir + File.separator + "output" + File.separator + videoId + "_final.mp4");

            if (file.exists()) {
                ctx.result(Files.newInputStream(file.toPath()));
                ctx.contentType("video/mp4");
                ctx.header("Content-Disposition", "attachment; filename=\"subtitleflow_video.mp4\"");
            } else {
                ctx.status(404).result("File not found on server.");
            }
        });

        System.out.println("Server is ready! Open http://localhost:8080 in your browser.");
    }
}