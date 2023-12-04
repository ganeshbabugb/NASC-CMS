package com.nasc.application.security;

import com.nasc.application.data.model.User;
import com.nasc.application.data.repository.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    private final SessionRegistry sessionRegistry;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository, SessionRegistry sessionRegistry) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
        this.sessionRegistry = sessionRegistry;
    }

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));
    }

    @Transactional
    public void logout() {
        // Get the current authentication
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Manually expire the session in the SessionRegistry
        expireUserSessions(userDetails);

        // Perform the Vaadin logout
        authenticationContext.logout();
    }

    private void expireUserSessions(UserDetails userDetails) {
        // Obtain the list of sessions for the user
        List<SessionInformation> userSessions = sessionRegistry.getAllSessions(userDetails, false);

        // Expire all sessions for the user
        userSessions.forEach(SessionInformation::expireNow);
    }

}
