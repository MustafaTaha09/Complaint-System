package com.example.complaintsystem.security;

import com.example.complaintsystem.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory; // Needed? No, BindAuthenticator is used below
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator; // Correct import
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider; // Correct import
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Needed for the mapper bean

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper(customUserDetailsService);
    }

    @Bean
    public AuthenticationProvider ldapAuthenticationProvider(
            BaseLdapPathContextSource contextSource,
            UserDetailsContextMapper userDetailsContextMapper,
            Environment env) {

        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        String userSearchFilter = env.getProperty("spring.security.ldap.user-search-filter");
        String userSearchBase = env.getProperty("spring.security.ldap.user-search-base", "");

        if (userSearchFilter == null || userSearchFilter.isBlank()) {
            throw new IllegalStateException("Missing LDAP configuration: 'spring.security.ldap.user-search-filter' must be set for bind authentication with user search.");
        } else {
            FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                    userSearchBase, userSearchFilter, contextSource);
            bindAuthenticator.setUserSearch(userSearch);
        }

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(bindAuthenticator);
        provider.setUserDetailsContextMapper(userDetailsContextMapper); // Use our mapper

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Correct lambda syntax
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/api-documentation/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/tickets/**").permitAll()
                        // .requestMatchers(HttpMethod.POST, "/users").permitAll() // Re-evaluate if this should be public
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}