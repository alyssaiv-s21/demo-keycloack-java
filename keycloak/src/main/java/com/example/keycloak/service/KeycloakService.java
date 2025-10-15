package com.example.keycloak.service;

import com.example.keycloak.config.KeycloakConfig;
import com.example.keycloak.model.TokenResponse;
import com.example.keycloak.model.User;
import com.example.keycloak.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final KeycloakConfig keycloakConfig;
    private final RestTemplate restTemplate;

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", keycloakConfig.getClientId());
        body.add("username", keycloakConfig.getUsername());
        body.add("password", keycloakConfig.getPassword());
        body.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                keycloakConfig.getAuthUrl(),
                request,
                TokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getAccessToken();
        } else {
            throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
        }
    }

    public List<User> getUsers(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = keycloakConfig.getUsersUrl();

        ResponseEntity<User[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                User[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return Arrays.asList(response.getBody());
        } else {
            throw new RuntimeException("Failed to get users: " + response.getStatusCode());
        }
    }

    public List<Group> getUserGroups(String accessToken, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = keycloakConfig.getUsersUrl() + "/" + userId + "/groups";

        ResponseEntity<Group[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Group[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return Arrays.asList(response.getBody());
        } else {
            throw new RuntimeException("Failed to get user groups: " + response.getStatusCode());
        }
    }

    public void printUsersWithGroups() {
        try {
            String accessToken = getAccessToken();
            System.out.println("Successfully obtained access token");

            List<User> users = getUsers(accessToken);
            System.out.println("Found " + users.size() + " users");

            for (User user : users) {
                List<Group> groups = getUserGroups(accessToken, user.getId());
                user.setGroups(groups);

                printUserInfo(user);
                System.out.println("----------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printUserInfo(User user) {
        System.out.println("User ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("First Name: " + (user.getFirstName() != null ? user.getFirstName() : "N/A"));
        System.out.println("Last Name: " + (user.getLastName() != null ? user.getLastName() : "N/A"));
        System.out.println("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
        System.out.println("Enabled: " + user.isEnabled());

        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            System.out.println("Groups:");
            for (Group group : user.getGroups()) {
                System.out.println("  - Group ID: " + group.getId() +
                        ", Name: " + group.getName() +
                        ", Path: " + group.getPath());
            }
        } else {
            System.out.println("Groups: No groups assigned");
        }
    }
}
