package com.mqv.monitor.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenFeignConfig {
    @Value("${monitor.spotify.client-id}")
    private String spotifyClientId;

    @Value("${monitor.spotify.client-secret}")
    private String spotifyClientSecret;

    @Bean(name = "spotify-basic-auth-interceptor")
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(spotifyClientId, spotifyClientSecret);
    }

    public String getSpotifyClientId() {
        return spotifyClientId;
    }
}
