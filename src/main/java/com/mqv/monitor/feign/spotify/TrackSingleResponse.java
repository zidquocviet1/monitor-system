package com.mqv.monitor.feign.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrackSingleResponse extends TrackResponse {
    @JsonProperty("preview_url")
    private String previewUrl;

    public TrackSingleResponse() {
    }

    public TrackSingleResponse(String id, String name, String uri, Long duration, String previewUrl) {
        super(id, name, uri, duration);
        this.previewUrl = previewUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
