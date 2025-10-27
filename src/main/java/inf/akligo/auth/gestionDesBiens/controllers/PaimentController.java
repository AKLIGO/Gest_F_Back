package inf.akligo.auth.gestionDesBiens.controllers;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutPaiement;
import org.springframework.security.access.prepost.PreAuthorize;
import inf.akligo.auth.gestionDesBiens.enumerateurs.ModePaiement;
import org.springframework.security.core.context.SecurityContextHolder;
import inf.akligo.auth.gestionDesBiens.entity.Paiement;
import inf.akligo.auth.gestionDesBiens.services.servicePaiement.PaiementService;
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
@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api/paiement")

@RequiredArgsConstructor



public class PaimentController{

 private final PaiementService paiementService;

  /**
     * Ajouter un paiement pour une réservation
     * @param reservationId l'identifiant de la réservation
     * @param montant montant payé
     * @param modePaiement mode de paiement choisi
     * @return le paiement enregistré
    */

    @PostMapping("/ajouter")
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
}