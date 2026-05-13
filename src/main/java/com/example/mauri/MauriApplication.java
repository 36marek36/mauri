package com.example.mauri;

import com.example.mauri.service.impl.MatchResultBackfillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MauriApplication {

	public static void main(String[] args) {
		SpringApplication.run(MauriApplication.class, args);
	}

    @Bean
    CommandLineRunner run(MatchResultBackfillService service) {
        return args -> service.fillMissingPoints();
    }

}
