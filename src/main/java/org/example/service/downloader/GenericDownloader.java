package org.example.service.downloader;

public class GenericDownloader implements VideoDownloader {

    @Override
    public String download(String url, String videoId, String lang) {
        System.out.println("GenericDownloader: Downloading video from generic site using standard algorithm...");
        return "/local/storage/" + videoId + "_generic.mp4";
    }
}