package org.example.domain.state;

import org.example.domain.VideoRequest;

public class WaitingForEditsState implements VideoState {
    @Override
    public void next(VideoRequest request) {
        System.out.println("State transition: Waiting for edits -> Rendering final video");
        request.setState(new CompletedState());
    }

    @Override
    public String getStatus() {
        return "WAITING_FOR_EDITS";
    }
}