package com.Reservation.ReservationService.Security;

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
    public static final String ADMIN = "client_admin";
    public static final String STUDENT = "client_student";
    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.
                authorizeHttpRequests(auth ->
                {
                    auth.requestMatchers(HttpMethod.GET,
                            "/actuator/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET,
                            "api/reservationAllReservationByStatus/*",
                            "api/reservation/AllReservation",
                            "api/reservation/getReservation/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.GET,
                            "api/reservation/AllReservationByStudentId/*").hasRole(STUDENT);
                    auth.requestMatchers(HttpMethod.POST,
                            "api/reservation/reservation/subject").hasRole(STUDENT);
                    auth.requestMatchers(HttpMethod.DELETE,
                            "api/reservation/delete/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.DELETE,
                            "api/reservation/delete-student-reservation/*").hasRole(STUDENT);
                    auth.requestMatchers(HttpMethod.PUT,
                            "api/reservation/approve-reservation/*").hasRole(ADMIN);

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
