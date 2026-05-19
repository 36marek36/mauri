package com.example.mauri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MauriApplication {

	public static void main(String[] args) {
		SpringApplication.run(MauriApplication.class, args);
	}

//    @Bean
//    CommandLineRunner run(MatchResultBackfillService service) {
//        return args -> service.fillMissingPoints();
//    }

}
