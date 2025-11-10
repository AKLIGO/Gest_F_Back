package inf.akligo.auth.gestionDesBiens.controllers;

import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.gestionDesBiens.services.serviceVehicules.ServiceVehicule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import inf.akligo.auth.gestionDesBiens.requests.VehiculeDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final ServiceVehicule serviceVehicule;

    // Ajouter un véhicule
    @PostMapping("/ajouter")
    public ResponseEntity<Vehicules> addVehicule(@RequestBody Vehicules vehicule) {
        Vehicules saved = serviceVehicule.addVehicules(vehicule);
        return ResponseEntity.ok(saved);
    }

    // Mettre à jour un véhicule
    @PutMapping("/modifier/{id}")
    public ResponseEntity<Vehicules> updateVehicule(@PathVariable Long id, @RequestBody Vehicules vehicule) {
        Vehicules updated = serviceVehicule.updateVehicules(vehicule, id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // Supprimer un véhicule
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> removeVehicule(@PathVariable Long id) {
        serviceVehicule.removeVehicules(id);
        return ResponseEntity.noContent().build();
    }

    // Changer le statut d'un véhicule
    @PatchMapping("/{id}/statut")
    public ResponseEntity<Vehicules> changerStatut(@PathVariable Long id, @RequestParam String statut) {
        Vehicules updated = serviceVehicule.changerStatutVehy(statut, id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // Lister tous les véhicules
    @GetMapping("/list")
    public ResponseEntity<List<Vehicules>> listVehicules() {
        return ResponseEntity.ok(serviceVehicule.listVehicules());
    }

    // Vérifier si un véhicule est disponible
    @GetMapping("/{id}/disponible")
    public ResponseEntity<Boolean> isDisponible(@PathVariable Long id) {
        return ResponseEntity.ok(serviceVehicule.isDiponible(id));
    }

    // Obtenir un véhicule par id
    @GetMapping("/{id}")
    public ResponseEntity<Vehicules> getVehiculeById(@PathVariable Long id) {
        try {
            Vehicules v = serviceVehicule.getVehiculesById(id);
            return ResponseEntity.ok(v);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

        // Lister tous les véhicules (DTO)
    @GetMapping("/lists")
    public ResponseEntity<List<VehiculeDTO>> listVehiculesDTO() {
        List<VehiculeDTO> vehiculesDTO = serviceVehicule.getAllVehiculesDTO();
        return ResponseEntity.ok(vehiculesDTO);
    }

    /**
     * Récupère tous les véhicules d'un propriétaire spécifique
     * @param proprietaireId L'ID du propriétaire
     * @return Liste des véhicules du propriétaire
     */
    @GetMapping("/proprietaire/{proprietaireId}")
    public ResponseEntity<List<VehiculeDTO>> getVehiculesByProprietaire(@PathVariable Long proprietaireId) {
        try {
            List<VehiculeDTO> vehicules = serviceVehicule.getVehiculesByProprietaire(proprietaireId);
            System.out.println("Récupération de " + vehicules.size() + " véhicules pour le propriétaire " + proprietaireId);
            return ResponseEntity.ok(vehicules);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des véhicules pour le propriétaire " + proprietaireId + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Récupère les véhicules du propriétaire connecté
     * @return Liste des véhicules du propriétaire connecté
     */
    @GetMapping("/mes-vehicules")
    public ResponseEntity<List<VehiculeDTO>> getMyVehicules() {
        try {
            List<VehiculeDTO> vehicules = serviceVehicule.getVehiculesByCurrentUser();
            System.out.println("Récupération de " + vehicules.size() + " véhicules pour l'utilisateur connecté");
            return ResponseEntity.ok(vehicules);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des véhicules de l'utilisateur connecté: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}/publication")
    public ResponseEntity<Vehicules> publierVehicule(@PathVariable Long id, @RequestParam boolean publie) {
        Vehicules updated = serviceVehicule.autoriserAffichage(id, publie);
        return ResponseEntity.ok(updated);
    }
}
