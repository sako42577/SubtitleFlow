package org.example;

import org.example.domain.SubtitleConfig;
import org.example.domain.Transcript;
import org.example.service.strategy.LowQualityStrategy;
import org.example.service.strategy.MediumQualityStrategy;
import org.example.service.strategy.QualityStrategyFactory;
import org.example.service.strategy.VideoQualityStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BusinessLogicTest {

    /**
     * Тест №1: Проверка паттерна Strategy для выбора качества видео.
     * Убеждаемся, что фабрика возвращает правильные стратегии и флаги для yt-dlp.
     */
    @Test
    public void testQualityStrategyFactory() {
        VideoQualityStrategy lowStrategy = QualityStrategyFactory.getStrategy("360p");
        Assertions.assertInstanceOf(LowQualityStrategy.class, lowStrategy, "Strategy for 360p should be LowQualityStrategy");
        Assertions.assertTrue(lowStrategy.getQualityFlag().contains("height<=360"), "Low quality flag must restrict height to 360");

        VideoQualityStrategy mediumStrategy = QualityStrategyFactory.getStrategy("720p");
        Assertions.assertInstanceOf(MediumQualityStrategy.class, mediumStrategy, "Strategy for 720p should be MediumQualityStrategy");
        Assertions.assertTrue(mediumStrategy.getQualityFlag().contains("height<=720"), "Medium quality flag must restrict height to 720");
    }

    /**
     * Тест №2: Проверка паттерна Builder для конфигурации субтитров.
     * Убеждаемся, что параметры корректно инкапсулируются в объект SubtitleConfig.
     */
    @Test
    public void testSubtitleConfigBuilder() {
        SubtitleConfig config = new SubtitleConfig.Builder()
                .setFontColor("yellow")
                .build();

        Assertions.assertNotNull(config, "Config object should not be null");
        Assertions.assertEquals("yellow", config.getFontColor(), "Font color should match the builder input");
    }

    /**
     * Тест №3: Проверка корректности обработки сырого текста субтитров (Transcript).
     * Проверяем, что сущность правильно хранит и отдает .srt разметку.
     */
    @Test
    public void testTranscriptEntity() {
        String sampleSrt = "1\n00:00:00,000 --> 00:00:05,000\nHello World";
        Transcript transcript = new Transcript(sampleSrt);

        Assertions.assertNotNull(transcript.getRawText(), "Transcript raw text should not be null");
        Assertions.assertEquals(sampleSrt, transcript.getRawText(), "Raw text must match stored SRT content");
        Assertions.assertTrue(transcript.getRawText().contains("00:00:05,000"), "Transcript must contain timestamp markers");
    }
}