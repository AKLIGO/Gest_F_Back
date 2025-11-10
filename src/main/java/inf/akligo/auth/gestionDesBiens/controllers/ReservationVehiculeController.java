package inf.akligo.auth.gestionDesBiens.controllers;

import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.requests.ReservationRequestVehi;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;
import inf.akligo.auth.gestionDesBiens.services.serviceReservation.ServiceReservation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;

@RestController
@RequestMapping("/api/reservations/vehicule")
@RequiredArgsConstructor
public class ReservationVehiculeController {

    private final ServiceReservation serviceReservation;

    // Créer une nouvelle réservation pour un véhicule
    @PostMapping
    public ResponseEntity<ReservationResponseVehi> createReservation(@RequestBody ReservationRequestVehi request) {
        ReservationResponseVehi reservation = serviceReservation.createReservationVehicule(request);
        return ResponseEntity.ok(reservation);
    }

    // Mettre à jour le statut d'une réservation
    @PutMapping("/{reservationId}/statut")
    public ResponseEntity<ReservationResponseVehi> updateReservationStatut(
            @PathVariable Long reservationId,
            @RequestParam String nouveauStatut
    ) {
        ReservationResponseVehi response = serviceReservation.updateReservationStatutVehi(reservationId, nouveauStatut);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicules")
    public List<ReservationResponseVehi> reservationsVehicules(){
        return serviceReservation.getReservationsVehicules();
    }

    /**
     * Récupère toutes les réservations de véhicules d'un propriétaire spécifique
     * @param proprietaireId L'ID du propriétaire
     * @return Liste des réservations de véhicules du propriétaire
     */
    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<List<ReservationResponseVehi>> getReservationsVehiculesByProprietaire(@PathVariable Long proprietaireId) {
        try {
            List<ReservationResponseVehi> reservations = serviceReservation.getReservationsVehiculesByProprietaire(proprietaireId);
            System.out.println("Récupération de " + reservations.size() + " réservations de véhicules pour le propriétaire " + proprietaireId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des réservations de véhicules pour le propriétaire " + proprietaireId + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Récupère les réservations de véhicules du propriétaire connecté
     * @return Liste des réservations de véhicules du propriétaire connecté
     */
    @GetMapping("/mes-reservations-vehicules")
    public ResponseEntity<List<ReservationResponseVehi>> getMyReservationsVehicules() {
        try {
            List<ReservationResponseVehi> reservations = serviceReservation.getReservationsVehiculesByCurrentUserP();
            System.out.println("Récupération de " + reservations.size() + " réservations de véhicules pour l'utilisateur connecté");
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des réservations de véhicules de l'utilisateur connecté: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 🔹 Réservations véhicules d’un utilisateur par ID
    @GetMapping("/vehicules/user/{userId}")
    public List<ReservationResponseVehi> getReservationsVehiculesByUser(@PathVariable Long userId) {
        return serviceReservation.getReservationsVehiculesByUser(userId);
    }

    // 🔹 Réservations véhicules de l’utilisateur connecté
    @GetMapping("/vehicules/me")
    public List<ReservationResponseVehi> getReservationsVehiculesCurrentUser() {
        return serviceReservation.getReservationsVehiculesByCurrentUser();
    }

    @GetMapping("/vehicules/mes")
    public List<ReservationResponseVehi> getReservationsVehiculesCurrentUserP() {
        return serviceReservation.getReservationsVehiculesByCurrentUser();
    }
}
