package inf.akligo.auth.gestionDesBiens.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private Long id;                  // ID de la réservation
    private LocalDate dateDebut;      // Date de début de la réservation
    private LocalDate dateFin;// Date de fin de la réservation
    private Double montant;        
    private String appartementNom;    // Nom de l'appartement
    private String appartementAdresse;// Adresse de l'appartement
    private String utilisateurNom;    // Nom de l'utilisateur
    private String utilisateurPrenoms;// Prénoms de l'utilisateur
    private String statut;            // Statut de la réservation (EN_ATTENTE, VALIDE, REJETE)
}