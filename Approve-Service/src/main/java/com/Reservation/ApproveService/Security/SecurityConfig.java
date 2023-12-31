package com.Reservation.ApproveService.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthConverter jwtAuthConverter;

    public static final String ADMIN = "client_admin";
    public static final String STUDENT = "client_student";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.
                authorizeHttpRequests(auth ->
                {
                    auth.requestMatchers(HttpMethod.GET,
                            "/actuator/**").permitAll();
                   auth.requestMatchers(HttpMethod.POST,
                    "api/approve/approve-reservation/*",
                           "api/approve/not-approve-reservation/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.GET,
                            "api/approve/all",
                            "api/approve/allConfirmed",
                            "api/approve/allDenied",
                            "api/approve/getById/*",
                            "api/approve/getApproveBySubjectCode/*",
                            "api/approve/getNotApproveBySubjectCode/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.GET,
                            "api/approve/getApproveOfStudent",
                            "api/approve/getNotApproveOfStudent").hasRole(STUDENT);
                    auth.requestMatchers(HttpMethod.DELETE,
                            "api/approve/delete/*").hasRole(ADMIN);

                    auth.anyRequest().authenticated();
                });

        http.
                oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)
                ));

        http.
                sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
