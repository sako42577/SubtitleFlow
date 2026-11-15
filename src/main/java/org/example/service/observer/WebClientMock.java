package org.example.service.observer;

import org.example.domain.VideoRequest;

// Имитация веб-сайта, который подписан на уведомления
public class WebClientMock implements JobSubscriber {

    private String clientName;

    public WebClientMock(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void onJobCompleted(VideoRequest request) {
        System.out.println("WebClient [" + clientName + "] received notification! Video ID " + request.getId() + " is ready for edits.");
    }
}