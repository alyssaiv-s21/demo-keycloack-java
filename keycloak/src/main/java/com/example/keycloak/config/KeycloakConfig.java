package com.example.keycloak.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {
    private String authUrl;
    private String usersUrl;
    private String clientId;
    private String username;
    private String password;
    private String realm;
}
