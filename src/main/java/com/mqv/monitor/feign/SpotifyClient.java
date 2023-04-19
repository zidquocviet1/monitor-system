package com.mqv.monitor.feign;

import com.mqv.monitor.feign.spotify.TrackListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("spotify")
public interface SpotifyClient {
    @GetMapping(path = "/v1/recommendations")
    TrackListResponse getRecommendationsTrack(@RequestParam(value = "market", defaultValue = "VN") String market,
                                              @RequestParam(value = "seed_artists") String seedArtists,
                                              @RequestParam(value = "seed_genres") String seedGenres,
                                              @RequestParam(value = "seed_tracks") String seedTracks,
                                              @RequestParam(value = "limit") int limit);

    default TrackListResponse getRecommendationsTrack() {
        return getRecommendationsTrack("VN", "4NHQUGzhtTLFvgF5SZesLK",
                "classical,country",  "0c6xIDDpzE81m2q797ordA", 10);
    }
}
