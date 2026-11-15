package org.example.service.observer;

import org.example.domain.VideoRequest;

// Интерфейс для всех, кто хочет слушать события сервера
public interface JobSubscriber {
    void onJobCompleted(VideoRequest request);
}