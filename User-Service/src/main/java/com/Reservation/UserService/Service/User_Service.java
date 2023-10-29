package com.Reservation.UserService.Service;

import com.Reservation.UserService.Dto.User;
import com.Reservation.UserService.Dto.UserTokenData;
import com.google.common.net.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class User_Service {
    private final WebClient.Builder webClientBuilder;

    public User_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private static final String KEYCLOAK_URL = "http://localhost:8081/realms/Subject_Reservation";

    public User UserInfo(String bearerToken){
        UserTokenData userTokenData = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/realms/Subject_Reservation/protocol/openid-connect/userinfo")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(UserTokenData.class)
                .block();

        assert userTokenData != null;

        return new User(userTokenData.getName(),
                userTokenData.getGiven_name(),
                userTokenData.getFamily_name(),
                userTokenData.getEmail(),
                userTokenData.getSub());
    }
}
