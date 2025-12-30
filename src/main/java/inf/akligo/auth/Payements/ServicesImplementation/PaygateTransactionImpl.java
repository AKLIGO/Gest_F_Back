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
        try {
            System.out.println("🔵 Début de la transaction pour : " + data.getPhone());
            
            // Construire la requête
            DepositRequestDto depositRequestDto = createDepositRequest(data);
            System.out.println("✅ Requête construite : " + depositRequestDto.getIdentifier());

            // Envoyer la requête et récupérer la réponse
            DepositResponseDto depositResponseDto = sendDepositRequest(depositRequestDto);
            System.out.println("✅ Réponse reçue : " + depositResponseDto.getTx_reference());

            // Sauvegarder la transaction initiale dans la base
            saveInitialDeposit(depositRequestDto, depositResponseDto);
            System.out.println("✅ Transaction sauvegardée en base");

            return depositResponseDto;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur dans depotTransactionPaygates : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'initialisation de la transaction : " + e.getMessage(), e);
        }
    }

    private DepositRequestDto createDepositRequest(ClientRequestDto clientRequestDto) {
        try {
            System.out.println("📝 Création de la requête de dépôt...");
            System.out.println("   - Téléphone: " + clientRequestDto.getPhone());
            System.out.println("   - Montant: " + clientRequestDto.getAmount());
            System.out.println("   - Réseau: " + clientRequestDto.getNetwork());
            
            return DepositRequestDto.builder()
                    .amount(clientRequestDto.getAmount())
                    .auth_token(AUTH_TOKEN)
                    .phone_number(clientRequestDto.getPhone())
                    .description("test application de Location")
                    .identifier(UUID.randomUUID().toString())
                    .network(clientRequestDto.getNetwork())
                    .build();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de la requête : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création de la requête de dépôt", e);
        }
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
            System.out.println("🔍 Vérification du statut de la transaction: " + data.getTx_reference());
            
            Mono<CheckResponseDTO> result = webClient
                    .post()
                    .uri("/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(data)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            res -> {
                                System.err.println("❌ Erreur 4xx lors de la vérification");
                                return Mono.error(new Exception("Erreur client PayGate"));
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            res -> {
                                System.err.println("❌ Erreur 5xx lors de la vérification");
                                return Mono.error(new Exception("Erreur serveur PayGate"));
                            })
                    .bodyToMono(CheckResponseDTO.class);

            CheckResponseDTO response = result.block();
            System.out.println("✅ Statut vérifié: " + response.getStatus());
            return response;

        } catch (WebClientResponseException e) {
            System.err.println("❌ Erreur HTTP: " + e.getStatusCode());
            throw new RuntimeException("Erreur HTTP : " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            throw new RuntimeException("Erreur inattendue lors de la vérification du statut de la transaction", e);
        }
    }
}
