package com.Reservation.SubjectService.Security;

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
                            "/actuator/**",
                            "api/subject/subjectCode/*",
                            "api/subject/subjectName/*",
                            "api/subject/AllSubjects",
                            "api/subject/instructor/firstname/*",
                            "api/subject/instructor/lastname/*",
                            "api/subject/instructor/expertise/*").permitAll();
                    auth.requestMatchers(HttpMethod.POST,
                            "api/subject/create-subject",
                            "api/subject/slotReduction/*").hasRole(STUDENT);
                    auth.requestMatchers(HttpMethod.DELETE,
                            "api/subject/delete/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.PUT,
                            "api/subject/update/*",
                            "api/subject/slotAddition/*").hasRole(ADMIN);
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
