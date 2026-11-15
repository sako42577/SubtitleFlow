package org.example.service.adapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleCommandAdapter {

    public String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            System.out.println("Adapter: Executing: " + command);

            // Используем bash, чтобы он правильно понимал кавычки и пробелы в путях файлов
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);

            // КРИТИЧЕСКИ ВАЖНО: Объединяем поток ошибок с обычным выводом, чтобы видеть ВСЁ!
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            System.out.println("Adapter: Command finished.");

        } catch (Exception e) {
            System.out.println("Adapter Error: Failed to execute command.");
            e.printStackTrace();
        }

        return output.toString();
    }
}