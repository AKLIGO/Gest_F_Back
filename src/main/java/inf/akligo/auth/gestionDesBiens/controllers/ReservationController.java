package inf.akligo.auth.gestionDesBiens.controllers;
import inf.akligo.auth.gestionDesBiens.services.serviceReservation.ServiceReservation;
import inf.akligo.auth.gestionDesBiens.requests.ReservationRequest;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseDTO;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;


@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ServiceReservation reservationService;

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponseDTO> updateReservationStatus(
            @PathVariable("id") Long reservationId,
            @RequestParam("statut") String nouveauStatut) {

        ReservationResponseDTO updatedReservation =
                reservationService.updateReservationStatus(reservationId, nouveauStatut);

        return ResponseEntity.ok(updatedReservation);
    }

    @GetMapping("/appartement/{appartementId}")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByAppartement(
            @PathVariable Long appartementId) {

        List<ReservationResponseDTO> reservations = reservationService.getReservationsByAppartement(appartementId);
        return ResponseEntity.ok(reservations);
    }

        // Endpoint pour récupérer la liste des réservations
    @GetMapping
    public List<ReservationResponseDTO> getReservations() {
        return reservationService.getAllReservations();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReservation(@PathVariable("id") Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.ok("Réservation supprimée avec succès !");
    }

    @GetMapping("/appartements")
    public List<ReservationResponseDTO> reservationsAppartements(){
        return reservationService.getReservationsAppartements();
    }

    /**
     * Récupère toutes les réservations d'un propriétaire spécifique
     * @param proprietaireId L'ID du propriétaire
     * @return Liste des réservations du propriétaire
     */
    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByProprietaire(@PathVariable Long proprietaireId) {
        try {
            List<ReservationResponseDTO> reservations = reservationService.getReservationsByProprietaire(proprietaireId);
            System.out.println("Récupération de " + reservations.size() + " réservations pour le propriétaire " + proprietaireId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des réservations pour le propriétaire " + proprietaireId + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Récupère les réservations du propriétaire connecté
     * @return Liste des réservations du propriétaire connecté
     */
    @GetMapping("/mes-reservations")
    public ResponseEntity<List<ReservationResponseDTO>> getMyReservations() {
        try {
            List<ReservationResponseDTO> reservations = reservationService.getReservationsByCurrentUser();
            System.out.println("Récupération de " + reservations.size() + " réservations pour l'utilisateur connecté");
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des réservations de l'utilisateur connecté: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }


}
