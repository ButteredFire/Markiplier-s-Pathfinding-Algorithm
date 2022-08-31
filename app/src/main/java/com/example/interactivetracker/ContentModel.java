package com.example.interactivetracker;

import org.json.JSONObject;

public class ContentModel {
    int fadeGradient;
    String videoID;

    String contentTitle;
    String contentStatistics;
    String contentThumbnail;

    // CONSTRUCTOR
    public ContentModel(int fadeGradient, String videoID, String contentTitle, String contentStatistics, String contentThumbnail) {
        this.fadeGradient = fadeGradient;
        this.videoID = videoID;

        this.contentTitle = contentTitle;
        this.contentStatistics = contentStatistics;
        this.contentThumbnail = contentThumbnail;
    }

    // GETTERS
    public int getFadeGradient() {
        return fadeGradient;
    }

    public String getVideoID() {
        return videoID;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentStatistics() {
        return contentStatistics;
    }

    public String getContentThumbnail() {
        return contentThumbnail;
    }


    // SETTERS
    public void setFadeGradient(int fadeGradient) { this.fadeGradient = fadeGradient; }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public void setContentStatistics(String contentStatistics) { this.contentStatistics = contentStatistics; }

    public void setContentThumbnail(String contentThumbnail) { this.contentThumbnail = contentThumbnail; }
}
