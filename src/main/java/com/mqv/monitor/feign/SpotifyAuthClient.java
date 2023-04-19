package com.mqv.monitor.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "spotify-auth")
public interface SpotifyAuthClient {
    @PostMapping(consumes = "application/x-www-form-urlencoded")
    AuthResponse getAccessToken(@RequestParam(value = "grant_type", defaultValue = "client_credentials") String grantType);

    default AuthResponse getAccessToken() {
        return getAccessToken("client_credentials");
    }

    record AuthResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") int expiresIn) {
    }
}
