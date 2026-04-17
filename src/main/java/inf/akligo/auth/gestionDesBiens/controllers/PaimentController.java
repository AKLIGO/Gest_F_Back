package inf.akligo.auth.gestionDesBiens.controllers;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutPaiement;
import org.springframework.security.access.prepost.PreAuthorize;
import inf.akligo.auth.gestionDesBiens.enumerateurs.ModePaiement;
import org.springframework.security.core.context.SecurityContextHolder;
import inf.akligo.auth.gestionDesBiens.entity.Paiement;
import inf.akligo.auth.gestionDesBiens.services.servicePaiement.PaiementService;
import inf.akligo.auth.gestionDesBiens.services.excel.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import inf.akligo.auth.gestionDesBiens.requests.PaiementDTO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
@CrossOrigin(origins = {"http://localhost:4200", "http://10.1.0.254:4200"}) 
@RestController
@RequestMapping("/api/paiement")

@RequiredArgsConstructor



public class PaimentController{

 private final PaiementService paiementService;
 private final ExcelExportService excelExportService;

  /**
     * Ajouter un paiement pour une réservation
     * @param reservationId l'identifiant de la réservation
     * @param montant montant payé
     * @param modePaiement mode de paiement choisi
     * @return le paiement enregistré
    */

    @PostMapping("/ajouter")
    // @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_PROPRIETAIRE', 'ROLE_ADMIN')")
    public ResponseEntity<Paiement> ajouterPaiement(
            @RequestParam Long reservationId,
            @RequestParam double montant,
            @RequestParam ModePaiement modePaiement) {

        Paiement paiement = paiementService.ajouterPaiement(reservationId, montant, modePaiement);
        return ResponseEntity.ok(paiement);
    }

        // Optionnel : récupérer tous les paiements
    @GetMapping("/tous")
    public ResponseEntity<List<PaiementDTO>> getTousPaiements() {
        return ResponseEntity.ok(paiementService.getAllPaiements());
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementByReservation(@PathVariable Long reservationId){
        List<PaiementDTO> paiements = paiementService.getPaiementByReservation(reservationId);
        return ResponseEntity.ok(paiements);
    }



    @PutMapping("/modifier/{id}")
    public Paiement modifierPaiement(@PathVariable Long id,
                                     @RequestParam(required = false) Double montant,
                                     @RequestParam(required = false) ModePaiement modePaiement,
                                     @RequestParam(required = false) StatutPaiement statut) {
        return paiementService.modifierPaiement(id, montant, modePaiement, statut);
    }

    // Supprimer un paiement
    @DeleteMapping("/supprimer/{id}")
    public void supprimerPaiement(@PathVariable Long id) {
        paiementService.supprimerPaiement(id);
    }

    /**
     * Exporte tous les paiements en fichier Excel
     * @return Fichier Excel avec la liste de tous les paiements
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportAllPaiementsToExcel() {
        try {
            List<PaiementDTO> paiements = paiementService.getAllPaiements();
            byte[] excelFile = excelExportService.exportAllPaiements(paiements);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "tous_les_paiements.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Exporte les paiements d'une réservation spécifique en fichier Excel
     * @param reservationId L'ID de la réservation
     * @return Fichier Excel avec la liste des paiements de la réservation
     */
    @GetMapping("/reservation/{reservationId}/export/excel")
    public ResponseEntity<byte[]> exportPaiementsByReservationToExcel(@PathVariable Long reservationId) {
        try {
            List<PaiementDTO> paiements = paiementService.getPaiementByReservation(reservationId);
            byte[] excelFile = excelExportService.exportPaiementsByReservation(paiements, reservationId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "paiements_reservation_" + reservationId + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}