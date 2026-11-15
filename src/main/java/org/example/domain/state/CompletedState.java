package org.example.domain.state;

import org.example.domain.VideoRequest;

public class CompletedState implements VideoState {
    @Override
    public void next(VideoRequest request) {
        System.out.println("Video is already completed. No next state.");
    }

    @Override
    public String getStatus() {
        return "COMPLETED";
    }
}