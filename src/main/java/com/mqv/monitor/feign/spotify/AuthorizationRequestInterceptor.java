package com.mqv.monitor.feign.spotify;

import com.mqv.monitor.config.OpenFeignConfig;
import com.mqv.monitor.feign.SpotifyAuthClient;
import com.mqv.monitor.redis.cache.SpotifyRedisCacheManager;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthorizationRequestInterceptor implements RequestInterceptor {
    private final SpotifyAuthClient spotifyAuthClient;
    private final SpotifyRedisCacheManager spotifyRedisCacheManager;
    private final OpenFeignConfig config;

    public AuthorizationRequestInterceptor(SpotifyAuthClient spotifyAuthClient,
                                           SpotifyRedisCacheManager spotifyRedisCacheManager, OpenFeignConfig config) {
        this.spotifyAuthClient = spotifyAuthClient;
        this.spotifyRedisCacheManager = spotifyRedisCacheManager;
        this.config = config;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Optional<SpotifyAuthClient.AuthResponse> authResponse = spotifyRedisCacheManager.get(config.getSpotifyClientId());
        String accessToken = "";

        if (authResponse.isEmpty()) {
            SpotifyAuthClient.AuthResponse response = spotifyAuthClient.getAccessToken();
            if (response != null) {
                spotifyRedisCacheManager.set(config.getSpotifyClientId(), response);
                accessToken = response.accessToken();
            }
        } else {
            accessToken = authResponse.get().accessToken();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        requestTemplate.header("Authorization", "Bearer " + accessToken);
    }
}
