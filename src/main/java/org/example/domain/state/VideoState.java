package org.example.domain.state;

import org.example.domain.VideoRequest;

public interface VideoState {
    void next(VideoRequest request);
    String getStatus();
}