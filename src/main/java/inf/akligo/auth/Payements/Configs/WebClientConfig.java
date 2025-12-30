package inf.akligo.auth.Payements.Configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://paygateglobal.com/api/v1")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
