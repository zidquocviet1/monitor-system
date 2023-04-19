package com.mqv.monitor.feign.spotify;

import java.util.List;

public class TrackListResponse {
    private List<TrackSingleResponse> tracks;

    public TrackListResponse() {
    }

    public List<TrackSingleResponse> getTracks() {
        return tracks;
    }

    public void setTracks(List<TrackSingleResponse> tracks) {
        this.tracks = tracks;
    }
}
