package com.Reservation.ApproveService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApproveServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApproveServiceApplication.class, args);
	}

}
