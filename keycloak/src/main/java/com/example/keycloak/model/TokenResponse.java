package com.example.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;
}
