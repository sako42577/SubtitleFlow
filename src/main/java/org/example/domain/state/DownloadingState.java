package org.example.domain.state;

import org.example.domain.VideoRequest;

public class DownloadingState implements VideoState {
    @Override
    public void next(VideoRequest request) {
        System.out.println("State transition: Downloading -> Waiting for user edits");
        request.setState(new WaitingForEditsState());
    }

    @Override
    public String getStatus() {
        return "DOWNLOADING";
    }
}