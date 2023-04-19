package com.mqv.monitor.controller;

import com.mqv.monitor.feign.SpotifyAuthClient;
import com.mqv.monitor.feign.SpotifyClient;
import com.mqv.monitor.feign.spotify.TrackListResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/spotify")
public class SpotifyController {
    private final SpotifyAuthClient spotifyAuthClient;
    private final SpotifyClient spotifyClient;

    public SpotifyController(SpotifyAuthClient spotifyAuthClient, SpotifyClient spotifyClient) {
        this.spotifyAuthClient = spotifyAuthClient;
        this.spotifyClient = spotifyClient;
    }

    @GetMapping
    @Bulkhead(name = "spotify", fallbackMethod = "handleGetAccessToken")
    public ResponseEntity<String> getAccessToken() {
        return ResponseEntity.ok(spotifyAuthClient.getAccessToken("client_credentials").accessToken());
    }

    @GetMapping("/recommendations")
    @Bulkhead(name = "spotify", fallbackMethod = "handleGetMetadata")
    public ResponseEntity<TrackListResponse> getSpotifyRecommendationTracks() {
        return ResponseEntity.ok(spotifyClient.getRecommendationsTrack());
    }

    private ResponseEntity<String> handleGetAccessToken(Throwable t) {
        return ResponseEntity.status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
                .body("Not allowed to get multiple access token");
    }

    private ResponseEntity<Void> handleGetMetadata(Throwable t) {
        return ResponseEntity.status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
                .build();
    }
}
