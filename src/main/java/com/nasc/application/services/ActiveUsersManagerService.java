package com.nasc.application.services;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActiveUsersManagerService {

    private final SessionRegistry sessionRegistry;

    public ActiveUsersManagerService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public List<UserDetails> getActiveUsers() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof UserDetails user)
                .map(principal -> (UserDetails) principal)
                .filter(user -> !sessionRegistry.getAllSessions(user, false).isEmpty())
                .collect(Collectors.toList());
    }

    //This method is used to ignore the current user form the active user list
    /*
    public List<UserDetails> getActiveUsers() {
        // Get the username of the current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof UserDetails user)
                .map(principal -> (UserDetails) principal)
                .filter(user -> !user.getUsername().equals(currentUsername)) // Exclude the current user
                .filter(user -> !sessionRegistry.getAllSessions(user, false).isEmpty())
                .collect(Collectors.toList());
    }
    */
}