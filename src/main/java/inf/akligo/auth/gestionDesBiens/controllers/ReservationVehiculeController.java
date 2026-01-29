package inf.akligo.auth.gestionDesBiens.controllers;

import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.requests.ReservationRequestVehi;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;
import inf.akligo.auth.gestionDesBiens.requests.CancellationInfoDTO;
import inf.akligo.auth.gestionDesBiens.services.serviceReservation.ServiceReservation;
import inf.akligo.auth.gestionDesBiens.services.excel.ExcelExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;

@RestController
@RequestMapping("/api/reservations/vehicule")
@RequiredArgsConstructor
public class ReservationVehiculeController {

    private final ServiceReservation serviceReservation;
    private final ExcelExportService excelExportService;

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

    /**
     * Met à jour une réservation de véhicule (dates, montant recalculé automatiquement)
     */
    @PutMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseVehi> updateReservationVehicule(
            @PathVariable Long reservationId,
            @RequestBody ReservationRequestVehi request
    ) {
        ReservationResponseVehi response = serviceReservation.updateReservationVehicule(reservationId, request);
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

    /**
     * Vérifie si une réservation de véhicule peut être annulée (dans les 24h)
     * @param reservationId L'ID de la réservation
     * @return true si l'annulation est possible
     */
    @GetMapping("/{id}/can-cancel")
    public ResponseEntity<Boolean> canCancelReservationVehicule(@PathVariable("id") Long reservationId) {
        try {
            boolean canCancel = serviceReservation.canCurrentUserCancelReservationVehicule(reservationId);
            return ResponseEntity.ok(canCancel);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    /**
     * Obtient les informations détaillées sur la possibilité d'annulation d'une réservation de véhicule
     * @param reservationId L'ID de la réservation
     * @return Détails sur la possibilité d'annulation
     */
    @GetMapping("/{id}/cancellation-info")
    public ResponseEntity<CancellationInfoDTO> getCancellationInfoVehicule(@PathVariable("id") Long reservationId) {
        try {
            CancellationInfoDTO info = serviceReservation.getCancellationInfoVehicule(reservationId);
            return ResponseEntity.ok(info);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                CancellationInfoDTO.builder()
                    .reservationId(reservationId)
                    .canCancel(false)
                    .message(e.getMessage())
                    .hoursRemaining(0L)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                CancellationInfoDTO.builder()
                    .reservationId(reservationId)
                    .canCancel(false)
                    .message("Erreur lors de la récupération des informations")
                    .hoursRemaining(0L)
                    .build()
            );
        }
    }

    /**
     * Annule une réservation de véhicule (dans les 24h après sa création)
     * @param reservationId L'ID de la réservation à annuler
     * @return La réservation annulée
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservationVehicule(@PathVariable("id") Long reservationId) {
        try {
            ReservationResponseVehi reservation = serviceReservation.cancelReservationVehicule(reservationId);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de l'annulation de la réservation de véhicule");
        }
    }

    /**
     * Exporte toutes les réservations de véhicules en fichier Excel
     * @return Fichier Excel avec la liste des réservations
     */
    @GetMapping("/vehicules/export/excel")
    public ResponseEntity<byte[]> exportReservationsVehiculesToExcel() {
        try {
            List<ReservationResponseVehi> reservations = serviceReservation.getReservationsVehicules();
            byte[] excelFile = excelExportService.exportReservationsVehicules(reservations);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reservations_vehicules.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Exporte les réservations de véhicules de l'utilisateur connecté en fichier Excel
     * @return Fichier Excel avec la liste des réservations
     */
    @GetMapping("/vehicules/me/export/excel")
    public ResponseEntity<byte[]> exportMyReservationsVehiculesToExcel() {
        try {
            List<ReservationResponseVehi> reservations = serviceReservation.getReservationsVehiculesByCurrentUser();
            byte[] excelFile = excelExportService.exportReservationsVehicules(reservations);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "mes_reservations_vehicules.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
