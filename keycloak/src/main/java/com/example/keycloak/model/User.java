package com.example.keycloak.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private List<Group> groups;
}
