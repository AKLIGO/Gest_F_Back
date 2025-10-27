package inf.akligo.auth.gestionDesBiens.controllers;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeFacture;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import inf.akligo.auth.gestionDesBiens.entity.Facture;
import inf.akligo.auth.gestionDesBiens.services.serviceFacture.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import inf.akligo.auth.gestionDesBiens.repository.FactureRepository;

import org.springframework.core.io.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;
@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api/facture")

@RequiredArgsConstructor

public class FactureController{
        private final FactureService factureService;
        private final FactureRepository factureRepository;
    /**
     * Générer une facture pour une réservation
     */
    @PostMapping("/generer")
    public ResponseEntity<Facture> genererFacture(
            @RequestParam Long reservationId,
            @RequestParam TypeFacture typeFacture,
            @RequestParam boolean solde
    ) throws Exception {
        Facture facture = factureService.genererFacture(reservationId, typeFacture, solde);
        return ResponseEntity.ok(facture);
    }


        /**
     * Télécharger le PDF d'une facture par l'ID de réservation
     */
    @GetMapping("/telecharger/{reservationId}")
    public ResponseEntity<Resource> telechargerFacture(@PathVariable Long reservationId) throws Exception {
        byte[] pdfData = factureService.getFacturePdf(reservationId);

        ByteArrayResource resource = new ByteArrayResource(pdfData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture_" + reservationId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(resource);
    }

    @GetMapping("/url/{reservationId}")
    public ResponseEntity<String> getFactureUrl(@PathVariable Long reservationId) {
        Facture facture = factureRepository.findByReservationId(reservationId)
                        .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
    
        String url = "http://localhost:8080/factures/files/" + facture.getCheminFichier();
        return ResponseEntity.ok(url);
    }


}