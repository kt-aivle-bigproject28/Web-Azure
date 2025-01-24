package com.bigproject.fic2toon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Fic2toonApplication {
	public static void main(String[] args) {
		SpringApplication.run(Fic2toonApplication.class, args);
	}
}
