package com.example.nyeondrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NyeonDriveApplication {

	public static void main(String[] args) {
		SpringApplication.run(NyeonDriveApplication.class, args);
	}

}
