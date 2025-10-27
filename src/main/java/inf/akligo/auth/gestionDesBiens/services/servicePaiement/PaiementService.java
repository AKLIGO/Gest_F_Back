package inf.akligo.auth.gestionDesBiens.services.servicePaiement;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;
import inf.akligo.auth.gestionDesBiens.requests.PaiementDTO;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import inf.akligo.auth.gestionDesBiens.repository.ReservationRepository;
import inf.akligo.auth.gestionDesBiens.repository.PaimentRepository;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.ModePaiement;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutPaiement;
import inf.akligo.auth.gestionDesBiens.entity.Paiement;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutDeReservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeDeRervation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService{
    private final ReservationRepository reservationRepository;
    private final PaimentRepository paiementRepository;

    public Paiement ajouterPaiement(Long reservartionId, double montant, ModePaiement modePaiement){

        Reservation reservation = reservationRepository.findById(reservartionId)
                    .orElseThrow( () -> new RuntimeException("Reservation non trouver"));
        
        Paiement paiement = Paiement.builder()
                .datePaiement(LocalDate.now())
                .montant(montant)
                .modePaiement(modePaiement)
                .statut(StatutPaiement.EFFECTUE)
                .reservation(reservation)
                .build();
        paiementRepository.save(paiement);

        /**
         * met a jour le statut de la reservation
         */
        double totalPaye = reservation.getMontantPaye();

        if(totalPaye >= reservation.getMontantPaye()){
            reservation.setStatut(StatutDeReservation.PAYEE);
        }
        else{
            reservation.setStatut(StatutDeReservation.PARTIELLEMENT_PAYEE);
        }

        reservationRepository.save(reservation);

        return paiement;
    

    }

    public List<PaiementDTO> getAllPaiements(){
        List<Paiement> paiements=paiementRepository.findAll();
        return paiements.stream()
                        .map(PaiementDTO::new) // transforme chaque Paiement en PaiementDTO
                        .collect(Collectors.toList());
    }

    public List<PaiementDTO> getPaiementByReservation(Long reservationId){
        /**
         * verifier si la reservation existe
         */
        
            Reservation reservation = reservationRepository.findById(reservationId)
                            .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

            // Récupère tous les paiements liés à cette réservation

            List<Paiement> paiements = paiementRepository.findByReservation(reservation);

               // Transforme en DTO
            return paiements.stream()
                    .map(PaiementDTO::new)
                    .collect(Collectors.toList());
        
    }



    @Transactional
    public Paiement modifierPaiement(Long paiementId, Double montant, ModePaiement modePaiement, StatutPaiement statut) {
    Paiement paiement = paiementRepository.findById(paiementId)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

    // Mettre à jour les champs seulement s'ils ne sont pas nuls
    if (montant != null) {
        paiement.setMontant(montant);
    }
    if (modePaiement != null) {
        paiement.setModePaiement(modePaiement);
    }
    if (statut != null) {
        paiement.setStatut(statut);
    }

    paiementRepository.save(paiement);

    // Optionnel : mettre à jour le statut de la réservation associée
    Reservation reservation = paiement.getReservation();
    double totalPaye = paiementRepository.findByReservation(reservation)
                        .stream()
                        .mapToDouble(Paiement::getMontant)
                        .sum();

    if (totalPaye >= reservation.getMontantPaye()) {
        reservation.setStatut(StatutDeReservation.PAYEE);
    } else if (totalPaye > 0) {
        reservation.setStatut(StatutDeReservation.PARTIELLEMENT_PAYEE);
    } else {
        reservation.setStatut(StatutDeReservation.EN_ATTENTE);
    }

    reservationRepository.save(reservation);

    return paiement;
}

    @Transactional
public void supprimerPaiement(Long paiementId) {
    Paiement paiement = paiementRepository.findById(paiementId)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

    Reservation reservation = paiement.getReservation();
    
    // Supprimer le paiement
    paiementRepository.delete(paiement);

    // Mettre à jour le statut de la réservation
    double totalPaye = paiementRepository.findByReservation(reservation)
                        .stream()
                        .mapToDouble(Paiement::getMontant)
                        .sum();

    if (totalPaye >= reservation.getMontantPaye()) {
        reservation.setStatut(StatutDeReservation.PAYEE);
    } else if (totalPaye > 0) {
        reservation.setStatut(StatutDeReservation.PARTIELLEMENT_PAYEE);
    } else {
        reservation.setStatut(StatutDeReservation.EN_ATTENTE);
    }

    reservationRepository.save(reservation);
}


}