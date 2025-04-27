package com.example.complaintsystem.security;

// --- Imports ---
import com.example.complaintsystem.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;

public class LdapUserDetailsMapper implements UserDetailsContextMapper {

    private static final Logger log = LoggerFactory.getLogger(LdapUserDetailsMapper.class);

    private final CustomUserDetailsService customUserDetailsService;

    public LdapUserDetailsMapper(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        log.debug("Mapping user from LDAP context for username: {}", username);
        try {
            UserDetails details = customUserDetailsService.loadUserByUsername(username);
            log.info("Successfully mapped LDAP authenticated user '{}' to local UserDetails", username);
            return details;
        } catch (UsernameNotFoundException e) {
            log.error("User '{}' authenticated via LDAP but not found in local database.", username, e);
            throw e;
        } catch (Exception e) {
            log.error("Error loading local UserDetails for LDAP authenticated user '{}'", username, e);
            throw new RuntimeException("Failed to map LDAP user to local context for user " + username, e);
        }
    }


    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        log.trace("WORKAROUND: mapUserToContext(UserDetails, DirContextAdapter) called (no-op) for user: {}", user.getUsername());
    }

}