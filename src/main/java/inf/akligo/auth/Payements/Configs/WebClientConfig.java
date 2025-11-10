package inf.akligo.auth.Payements.Configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // Tu peux définir ici l’URL de base de ton service PayGate/TMoney
        return WebClient.builder()
                .baseUrl("https://paygateglobal.com/api") // ou ton URL locale
                .build();
    }
}
