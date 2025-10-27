package inf.akligo.auth.securityConfig.permitionPath;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import inf.akligo.auth.securityConfig.security.JwtFilter;
import inf.akligo.auth.securityConfig.confBeans.BeansConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;



@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;

    private final AuthenticationProvider authenticationProvider;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll() // Autoriser aussi toutes les requêtes sous /public
            .requestMatchers("/auth/users/me").hasRole("USER") // Protection pour cette route spécifique
            .requestMatchers("/api/appartement/").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/immeubles").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/image/").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/image/ajoutImageTovehicule").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/paiement/supprimer/{id}").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/vehicules/ajouter").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/vehicules/supprimer/{id}").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/vehicules/modifier/{id}").hasAnyRole("PROPRIETAIRE", "ADMIN")
            .requestMatchers("/api/vehicules/list").permitAll()
            .requestMatchers("/api/vehicules/lists").permitAll()
            .requestMatchers("/api/vehicules/proprietaire/**").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/vehicules/mes-vehicules").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/appartement/proprietaire/**").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/appartement/mes-appartements").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/reservations/proprietaire/**").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/reservations/mes-reservations").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/reservations/vehicule/proprietaire/**").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/reservations/vehicule/mes-reservations-vehicules").hasAnyRole("PROPRIETAIRE", "ADMIN", "USER")
            .requestMatchers("/api/auth/**").permitAll() // Autoriser toutes les requêtes sous /auth
            .requestMatchers("/api/image/file/**").permitAll()
            .requestMatchers("/api/appartement/list").permitAll()
            .requestMatchers("/api/appartement/lists").permitAll()
            .requestMatchers("/api/image/libres").permitAll()
            .requestMatchers("/api/paiement/**").authenticated()
            .anyRequest().authenticated() // Toute autre requête nécessite une authentification
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}
