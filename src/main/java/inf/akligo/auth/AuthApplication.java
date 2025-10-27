package inf.akligo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.entity.Roles;

import inf.akligo.auth.authConfiguration.servicesCompte.ServiceCompte;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

	// @Bean
	// public CommandLineRunner runner(ServiceCompte serviceCompte){

	// 	return args -> {
	// 		serviceCompte.ajouterRole(new Roles(null,"USER"));
	// 		serviceCompte.ajouterRole(new Roles(null,"PROPRIETAIRE"));
	// 		serviceCompte.ajouterRole(new Roles(null,"ADMINISTRATEUR"));

			
	// 	};

	// }

}
