package org.example.service;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class SubtitleChunker {

    public static String processVttFile(File vttFile) {
        try {
            List<String> lines = Files.readAllLines(vttFile.toPath());
            StringBuilder result = new StringBuilder("WEBVTT\n\n");

            for (String line : lines) {
                line = line.trim();
                // Пропускаем пустые строки, заголовки и цифровые индексы
                if (line.isEmpty() || line.equals("WEBVTT") || line.matches("^\\d+$")) {
                    continue;
                }

                // Если это таймкод (содержит -->), оставляем как есть
                if (line.contains("-->")) {
                    result.append("\n").append(line).append("\n");
                } else {
                    // Если строка слишком длинная, режем её на две части для удобства чтения
                    if (line.length() > 50) {
                        int mid = line.lastIndexOf(' ', 50);
                        if (mid == -1) mid = 50;
                        result.append(line.substring(0, mid).trim()).append("\n");
                        result.append(line.substring(mid).trim()).append("\n");
                    } else {
                        result.append(line).append("\n");
                    }
                }
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error formatting VTT.";
        }
    }
}