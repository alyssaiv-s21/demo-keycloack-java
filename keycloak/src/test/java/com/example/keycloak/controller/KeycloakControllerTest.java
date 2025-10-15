package com.example.keycloak.controller;

import com.example.keycloak.service.KeycloakService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(KeycloakController.class)
@ExtendWith(MockitoExtension.class)
class KeycloakControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KeycloakService keycloakService;

    @Test
    @WithMockUser
    void shouldGetUsersAndCallService() throws Exception {

        doNothing().when(keycloakService).printUsersWithGroups();

        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(content().string("Check console for user information"));

        verify(keycloakService, times(1)).printUsersWithGroups();
    }
}
