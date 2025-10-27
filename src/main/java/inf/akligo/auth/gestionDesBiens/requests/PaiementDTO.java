package inf.akligo.auth.gestionDesBiens.requests;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;

//import inf.akligo.auth.gestionDesBiens.requests.ImageDTO;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutPaiement; 
import inf.akligo.auth.gestionDesBiens.enumerateurs.ModePaiement; 
import inf.akligo.auth.gestionDesBiens.entity.Paiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PaiementDTO{
     private Long id;
    private double montant;
    private LocalDate datePaiement;
    private ModePaiement modePaiement;
    private StatutPaiement statut;
    private Long reservationId;
    private Long utilisateurId;
    private String utilisateurNom;      // nouveau
    private String utilisateurTelephone; // nouveau

    // constructeur depuis Paiement
    public PaiementDTO(Paiement paiement) {
        this.id = paiement.getId();
        this.montant = paiement.getMontant();
        this.datePaiement = paiement.getDatePaiement();
        this.modePaiement = paiement.getModePaiement();
        this.statut = paiement.getStatut();

        if(paiement.getReservation() != null){
            this.reservationId = paiement.getReservation().getId();
            if(paiement.getReservation().getUtilisateur() != null){
                this.utilisateurId = paiement.getReservation().getUtilisateur().getId();
                this.utilisateurNom = paiement.getReservation().getUtilisateur().getNom(); // ou getFullName() selon ton modèle
                this.utilisateurTelephone = paiement.getReservation().getUtilisateur().getTelephone();
            }
        }
    }
}