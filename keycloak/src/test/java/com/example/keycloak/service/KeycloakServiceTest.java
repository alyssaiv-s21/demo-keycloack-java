package com.example.keycloak.service;

import com.example.keycloak.config.KeycloakConfig;
import com.example.keycloak.model.Group;
import com.example.keycloak.model.TokenResponse;
import com.example.keycloak.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    private KeycloakConfig keycloakConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KeycloakService keycloakService;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;


    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void getAccessToken_shouldReturnToken_onSuccessfulRequest() {

        String authUrl = "http://keycloak/auth";
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("test_access_token");

        when(keycloakConfig.getAuthUrl()).thenReturn(authUrl);
        when(restTemplate.postForEntity(eq(authUrl), any(), eq(TokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));


        String accessToken = keycloakService.getAccessToken();


        assertThat(accessToken).isEqualTo("test_access_token");
        verify(restTemplate, times(1)).postForEntity(eq(authUrl), any(), eq(TokenResponse.class));
    }

    @Test
    void getAccessToken_shouldThrowException_onFailedRequest() {

        String authUrl = "http://keycloak/auth";
        when(keycloakConfig.getAuthUrl()).thenReturn(authUrl);
        when(restTemplate.postForEntity(eq(authUrl), any(), eq(TokenResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));


        assertThatThrownBy(() -> keycloakService.getAccessToken())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get access token");
    }

    @Test
    void getUsers_shouldReturnListOfUsers_onSuccessfulRequest() throws Exception {

        String usersUrl = "http://keycloak/users";
        User[] usersArray = {
                createUser("1", "user1", "John", "Doe", "john.doe@example.com", true),
                createUser("2", "user2", "Jane", "Doe", "jane.doe@example.com", false)
        };
        List<User> expectedUsers = List.of(usersArray);

        when(keycloakConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.exchange(eq(usersUrl), eq(HttpMethod.GET), any(), eq(User[].class)))
                .thenReturn(new ResponseEntity<>(usersArray, HttpStatus.OK));


        List<User> actualUsers = keycloakService.getUsers("test_token");


        assertThat(actualUsers).isEqualTo(expectedUsers);
    }

    @Test
    void getUsers_shouldThrowException_onFailedRequest() {

        String usersUrl = "http://keycloak/users";
        when(keycloakConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.exchange(eq(usersUrl), eq(HttpMethod.GET), any(), eq(User[].class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));


        assertThatThrownBy(() -> keycloakService.getUsers("test_token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get users");
    }

    @Test
    void getUserGroups_shouldReturnListOfGroups_onSuccessfulRequest() throws Exception {

        String userId = "123";
        String usersUrl = "http://keycloak/users";
        String groupsUrl = usersUrl + "/" + userId + "/groups";
        Group[] groupsArray = {createGroup("group1", "Group One", "/path/to/group1")};
        List<Group> expectedGroups = List.of(groupsArray);

        when(keycloakConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.exchange(eq(groupsUrl), eq(HttpMethod.GET), any(), eq(Group[].class)))
                .thenReturn(new ResponseEntity<>(groupsArray, HttpStatus.OK));


        List<Group> actualGroups = keycloakService.getUserGroups("test_token", userId);


        assertThat(actualGroups).isEqualTo(expectedGroups);
    }

    @Test
    void getUserGroups_shouldThrowException_onFailedRequest() {

        String userId = "123";
        String usersUrl = "http://keycloak/users";
        String groupsUrl = usersUrl + "/" + userId + "/groups";
        when(keycloakConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.exchange(eq(groupsUrl), eq(HttpMethod.GET), any(), eq(Group[].class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));


        assertThatThrownBy(() -> keycloakService.getUserGroups("test_token", userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get user groups");
    }

    @Test
    void printUsersWithGroups_shouldLogCorrectly_onSuccessfulCalls() throws Exception {

        String accessToken = "test_access_token";
        String authUrl = "http://keycloak/auth";
        String usersUrl = "http://keycloak/users";
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken);

        User[] usersArray = {createUser("1", "user1", null, null, null, true)};
        Group[] groupsArray = {createGroup("group1", "Group One", "/path")};

        when(keycloakConfig.getAuthUrl()).thenReturn(authUrl);
        when(keycloakConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.postForEntity(eq(authUrl), any(), eq(TokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(eq(usersUrl), eq(HttpMethod.GET), any(), eq(User[].class)))
                .thenReturn(new ResponseEntity<>(usersArray, HttpStatus.OK));
        when(restTemplate.exchange(eq(usersUrl + "/1/groups"), eq(HttpMethod.GET), any(), eq(Group[].class)))
                .thenReturn(new ResponseEntity<>(groupsArray, HttpStatus.OK));

        keycloakService.printUsersWithGroups();

        String output = outContent.toString();
        assertTrue(output.contains("Successfully obtained access token"));
        assertTrue(output.contains("Found 1 users"));
        assertTrue(output.contains("User ID: 1"));
        assertTrue(output.contains("Groups:"));
        assertTrue(output.contains("  - Group ID: group1, Name: Group One, Path: /path"));
    }

    @Test
    void printUsersWithGroups_shouldHandleException_andLogError() throws Exception {
        // Arrange
        String authUrl = "http://keycloak/auth";
        when(keycloakConfig.getAuthUrl()).thenReturn(authUrl);
        when(restTemplate.postForEntity(eq(authUrl), any(), eq(TokenResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));


        keycloakService.printUsersWithGroups();

        String output = outContent.toString();
        assertTrue(output.contains("Error: "));
    }

    private User createUser(String id, String username, String firstName, String lastName, String email, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEnabled(enabled);
        return user;
    }

    private Group createGroup(String id, String name, String path) {
        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setPath(path);
        return group;
    }
}










