package inf.akligo.auth.Payements.ServicesImplementation;

import inf.akligo.auth.Payements.dtos.*;
import inf.akligo.auth.Payements.entities.DepositRequestPaygates;
import inf.akligo.auth.Payements.repositories.DepositRequestDao;
import inf.akligo.auth.Payements.services.PaygateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AllArgsConstructor
@Transactional
@Service
public class PaygateTransactionImpl implements PaygateService {

    private final WebClient webClient;
    private final DepositRequestDao depositRequestDao;

    private static final String AUTH_TOKEN = "0f4b917b-4923-4bbb-9071-24be4dfedb1f";

    @Override
    public Object depotransaction(Object data) {
        try {
            Mono<Object> result = webClient
                    .post()
                    .uri("/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + AUTH_TOKEN)
                    .bodyValue(data)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            res -> Mono.error(new Exception("Erreur client TMoney")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            res -> Mono.error(new Exception("Erreur serveur TMoney")))
                    .bodyToMono(Object.class);

            return result.block();

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erreur HTTP : " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue pendant la transaction", e);
        }
    }

    @Override
    public DepositResponseDto depotTransactionPaygates(ClientRequestDto data) {
        // Construire la requête
        DepositRequestDto depositRequestDto = createDepositRequest(data);

        // Envoyer la requête et récupérer la réponse
        DepositResponseDto depositResponseDto = sendDepositRequest(depositRequestDto);

        // Sauvegarder la transaction initiale dans la base
        saveInitialDeposit(depositRequestDto, depositResponseDto);

        return depositResponseDto;
    }

    private DepositRequestDto createDepositRequest(ClientRequestDto clientRequestDto) {
        return DepositRequestDto.builder()
                .amount(clientRequestDto.getAmount())
                .auth_token(AUTH_TOKEN)
                .phone_number(clientRequestDto.getPhone())
                .description("test application de Location")
                .identifier(UUID.randomUUID().toString())
                .network(clientRequestDto.getNetwork())
                .build();
    }

    private DepositResponseDto sendDepositRequest(DepositRequestDto depositRequestDto) {
        try {
            return webClient
                    .post()
                    .uri("/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(depositRequestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            res -> Mono.error(new Exception("Erreur client TMoney")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            res -> Mono.error(new Exception("Erreur serveur TMoney")))
                    .bodyToMono(DepositResponseDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erreur HTTP : " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de l’envoi de la requête de dépôt", e);
        }
    }

    private void saveInitialDeposit(DepositRequestDto depositRequestDto, DepositResponseDto depositResponseDto) {
        DepositRequestPaygates depositRequestPaygates = DepositRequestPaygates.builder()
                .phone_number(depositRequestDto.getPhone_number())
                .amount(depositRequestDto.getAmount())
                .description(depositRequestDto.getDescription())
                .identifier(depositRequestDto.getIdentifier())
                .network(depositRequestDto.getNetwork())
                .tx_reference(depositResponseDto.getTx_reference())
                .status(2)
                .build();

        depositRequestDao.save(depositRequestPaygates);

        System.out.println("✅ Transaction enregistrée pour " + depositRequestDto.getPhone_number());
    }

    @Override
    public CheckResponseDTO checkTransactionStatus(CheckTransactionDto data) {
        try {
            Mono<CheckResponseDTO> result = webClient
                    .post()
                    .uri("/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(data)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            res -> Mono.error(new Exception("Erreur client TMoney")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            res -> Mono.error(new Exception("Erreur serveur TMoney")))
                    .bodyToMono(CheckResponseDTO.class);

            return result.block();

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erreur HTTP : " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la vérification du statut de la transaction", e);
        }
    }
}
