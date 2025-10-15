package com.example.keycloak.controller;

import com.example.keycloak.service.KeycloakService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KeycloakController {

    private final KeycloakService keycloakService;

    public KeycloakController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @GetMapping("/users")
    public String getUsers() {
        keycloakService.printUsersWithGroups();
        return "Check console for user information";
    }
}
