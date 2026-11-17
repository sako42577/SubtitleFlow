package org.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final Map<String, VideoRequest> database = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        WebClientMock myBrowser = new WebClientMock("Chrome_User_1");
        ConsoleCommandAdapter adapter = new ConsoleCommandAdapter();
        Javalin app = startServer();

        // 1. Process URL + Settings: Download video & run Whisper with auto-detection
        app.post("/api/process", ctx -> processVideo(ctx, adapter));

        // 2. Approve Subtitles & Render via FFmpeg (with custom Font Size & Color)
        app.post("/api/approve", ctx -> approveSubtitles(ctx, adapter));

        // 3. Browser File Download Endpoint
        app.get("/api/download/{id}", Main::downloadFinalVideo);

        System.out.println("Server is ready! Open http://localhost:8080 in your browser.");
    }

    private static Javalin startServer() {
        return Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(8080);
    }

    private static void processVideo(Context ctx, ConsoleCommandAdapter adapter) {
        Map<String, String> body = ctx.bodyAsClass(Map.class);
        String url = body.get("originalUrl");
        String quality = body.getOrDefault("videoQuality", "720p");
        String fontSize = body.getOrDefault("fontSize", "24");
        String color = body.getOrDefault("fontColor", "white");

        VideoRequest request = createRequest(url, color);
        File outputDir = getOutputDirectory();
        if (!outputDir.exists()) outputDir.mkdirs();

        try {
            downloadVideo(request, quality);
            File videoFile = findVideoFile(outputDir, request.getId(), "Downloaded video file not found.");
            runWhisper(videoFile, outputDir, adapter);

            request.setDraftTranscript(readTranscript(videoFile, outputDir));
            database.put(request.getId(), request);
            ctx.json(request);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error during processing: " + e.getMessage());
        }
    }

    private static void approveSubtitles(Context ctx, ConsoleCommandAdapter adapter) {
        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        String rawText = (String) body.get("rawText");
        String fontSize = (String) body.getOrDefault("fontSize", "24");
        String videoId = ctx.queryParam("id");

        VideoRequest request = database.get(videoId);
        if (request == null) {
            ctx.status(404).result("Error: Video not found in database");
            return;
        }

        File outputDir = getOutputDirectory();
        String subFilePath = outputDir.getAbsolutePath() + File.separator + videoId + ".srt";
        String finalVideoPath = outputDir.getAbsolutePath() + File.separator + videoId + "_final.mp4";

        try {
            File originalVideo = findVideoFile(outputDir, videoId, "Original video file not found!");
            writeSubtitles(subFilePath, rawText);
            String colorCode = getSubtitleColorCode(request);
            renderVideo(originalVideo, subFilePath, finalVideoPath, colorCode, fontSize, adapter);

            ProcessedMedia finalMedia = new ProcessedMedia(finalVideoPath);
            ctx.json(finalMedia);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error during rendering: " + e.getMessage());
        }
    }

    private static void downloadFinalVideo(Context ctx) throws IOException {
        String videoId = ctx.pathParam("id");
        File file = new File(getOutputDirectory().getAbsolutePath()
                + File.separator + videoId + "_final.mp4");

        if (file.exists()) {
            ctx.result(Files.newInputStream(file.toPath()));
            ctx.contentType("video/mp4");
            ctx.header("Content-Disposition", "attachment; filename=\"subtitleflow_video.mp4\"");
        } else {
            ctx.status(404).result("File not found on server.");
        }
    }

    private static VideoRequest createRequest(String url, String color) {
        String uniqueId = UUID.randomUUID().toString();
        VideoRequest request = new VideoRequest(uniqueId, url);

        SubtitleConfig config = new SubtitleConfig.Builder()
                .setFontColor(color)
                .build();
        request.setConfig(config);

        return request;
    }

    private static File getOutputDirectory() {
        String projectDir = new File("").getAbsolutePath();
        return new File(projectDir + File.separator + "output");
    }

    private static void downloadVideo(VideoRequest request, String quality) {
        // Скачиваем видео в выбранном качестве (Strategy Pattern)
        YoutubeDownloader downloader = new YoutubeDownloader();
        System.out.println("Downloading video with quality strategy: " + quality);
        downloader.downloadVideoWithQuality(request.getOriginalUrl(), request.getId(), quality);
    }

    private static File findVideoFile(File outputDir, String videoId, String errorMessage) throws Exception {
        File[] videoFiles = outputDir.listFiles((d, name) ->
                name.startsWith(videoId) &&
                        !name.endsWith(".srt") &&
                        !name.endsWith(".vtt") &&
                        !name.endsWith(".mp3") &&
                        !name.contains("_final")
        );

        if (videoFiles == null || videoFiles.length == 0) {
            throw new Exception(errorMessage);
        }

        return videoFiles[0];
    }

    private static void runWhisper(File videoFile, File outputDir, ConsoleCommandAdapter adapter) {
        // Запускаем Whisper без принудительного языка (AI сам определит язык речи)
        System.out.println("Running Whisper AI with auto language detection...");
        String whisperCommand = "/home/sanzhar/.local/bin/whisper \"" + videoFile.getAbsolutePath()
                + "\" --model tiny --output_format srt --output_dir \"" + outputDir.getAbsolutePath() + "\"";

        adapter.executeCommand(whisperCommand);
    }

    private static Transcript readTranscript(File videoFile, File outputDir) throws IOException {
        String baseName = videoFile.getName().substring(0, videoFile.getName().lastIndexOf('.'));
        File generatedSrt = new File(outputDir.getAbsolutePath() + File.separator + baseName + ".srt");

        String rawText = "";
        if (generatedSrt.exists()) {
            rawText = Files.readString(generatedSrt.toPath());
        } else {
            rawText = "1\n00:00:00,000 --> 00:00:05,000\n[Transcription warning: No subtitles generated]";
        }

        return new Transcript(rawText);
    }

    private static void writeSubtitles(String subFilePath, String rawText) throws IOException {
        FileWriter writer = new FileWriter(subFilePath);
        writer.write(rawText);
        writer.close();
    }

    private static String getSubtitleColorCode(VideoRequest request) {
        // Цвет субтитров
        String colorCode = "&H00FFFFFF"; // White default
        if (request.getConfig() != null) {
            String chosenColor = request.getConfig().getFontColor();
            if ("yellow".equals(chosenColor)) colorCode = "&H0000FFFF";
            if ("green".equals(chosenColor)) colorCode = "&H0000FF00";
            if ("red".equals(chosenColor)) colorCode = "&H000000FF";
        }

        return colorCode;
    }

    private static void renderVideo(File originalVideo, String subFilePath, String finalVideoPath,
                                    String colorCode, String fontSize, ConsoleCommandAdapter adapter) {
        // FFmpeg вшивает субтитры с динамическим размером шрифта (FontSize)
        String ffmpegCommand = "ffmpeg -y -i \"" + originalVideo.getAbsolutePath()
                + "\" -vf \"subtitles=" + subFilePath
                + ":force_style='PrimaryColour=" + colorCode + ",FontSize=" + fontSize
                + "'\" -c:v libx264 -c:a aac \"" + finalVideoPath + "\"";

        System.out.println("Running FFmpeg burn-in with Font Size: " + fontSize);
        adapter.executeCommand(ffmpegCommand);
    }
}
