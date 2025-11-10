package inf.akligo.auth.gestionDesBiens.controllers;
import inf.akligo.auth.gestionDesBiens.requests.AppartementDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.services.serviceApp.ServiceApp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api/appartement")

@RequiredArgsConstructor
public class AppartementController {
    private static final Logger log = LoggerFactory.getLogger(AppartementController.class);
    private final ServiceApp serviceApp;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, 
             produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Appartement> addAppartement(@Valid @RequestBody Appartement appartement) {
        System.out.println("Tentative d'ajout d'un appartement par l'utilisateur : " + SecurityContextHolder.getContext().getAuthentication().getName());
        Appartement appartementSave = serviceApp.addAppartement(appartement);
        return ResponseEntity.ok(appartementSave);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appartement> modifierApp(@Valid @RequestBody Appartement appartement, @PathVariable Long id) {
        Appartement updatedApp = serviceApp.updateAppartement(appartement, id);
        return ResponseEntity.ok(updatedApp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerApp(@PathVariable Long id) {
        serviceApp.removeAppartement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Appartement>> getAllAppartements() {
        return ResponseEntity.ok(serviceApp.listAppartement());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appartement> getAppartementById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceApp.getAppartementById(id));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Appartement> changerStatut(@PathVariable Long id, @RequestParam String statut) {
        Appartement updated = serviceApp.changerStatutApp(statut, id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/disponible")
    public ResponseEntity<Boolean> isDisponible(@PathVariable Long id) {
        boolean disponible = serviceApp.isDiponible(id);
        return ResponseEntity.ok(disponible);
    }
  @GetMapping("/list")
 public ResponseEntity<List<AppartementDTO>> getAllAppartementsDTO() {
    List<AppartementDTO> appartements = serviceApp.getAllAppartementsDTO();
    if (appartements.isEmpty()) {
        System.out.println("Aucun appartement trouvé.");
    }
    return ResponseEntity.ok(appartements);
}

    /**
     * Récupère tous les appartements d'un propriétaire spécifique
     * @param proprietaireId L'ID du propriétaire
     * @return Liste des appartements du propriétaire
     */
    @GetMapping("/proprietaire/{proprietaireId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<List<AppartementDTO>> getAppartementsByProprietaire(@PathVariable Long proprietaireId) {
        try {
            List<AppartementDTO> appartements = serviceApp.getAppartementsByProprietaire(proprietaireId);
            log.info("Récupération de {} appartements pour le propriétaire {}", appartements.size(), proprietaireId);
            return ResponseEntity.ok(appartements);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des appartements pour le propriétaire {}: {}", proprietaireId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les appartements du propriétaire connecté
     * @return Liste des appartements du propriétaire connecté
     */
    @GetMapping("/mes-appartements")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<List<AppartementDTO>> getMyAppartements() {
        try {
            List<AppartementDTO> appartements = serviceApp.getAppartementsByCurrentUser();
            log.info("Récupération de {} appartements pour l'utilisateur connecté", appartements.size());
            return ResponseEntity.ok(appartements);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des appartements de l'utilisateur connecté: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/publication")
    public ResponseEntity<Appartement> publierAppartement(@PathVariable Long id, @RequestParam boolean publie) {
        Appartement updated = serviceApp.autoriserAffichage(id, publie);
        return ResponseEntity.ok(updated);
    }


}
