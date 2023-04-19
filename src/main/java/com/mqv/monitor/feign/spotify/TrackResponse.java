package com.mqv.monitor.feign.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrackResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("uri")
    private String uri;
    @JsonProperty("duration_ms")
    private Long duration;

    public TrackResponse() {
    }

    public TrackResponse(String id, String name, String uri, Long duration) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}