package inf.akligo.auth.authConfiguration.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import inf.akligo.auth.authConfiguration.servicesCompte.ProprietaireService;
import inf.akligo.auth.authConfiguration.datas.ProprietaireContactDTO;

@RestController
public class ProprietaireController {

    private final ProprietaireService proprietaireService;

    public ProprietaireController(ProprietaireService proprietaireService) {
        this.proprietaireService = proprietaireService;
    }

    // 🔹 Obtenir le contact du propriétaire d'un appartement
    @GetMapping("/api/proprio/appartement/{id}")
    public ProprietaireContactDTO getContactAppartement(@PathVariable("id") Long appartementId) {
        return proprietaireService.getProprietaireContactAppartement(appartementId);
    }

    // 🔹 Obtenir le contact du propriétaire d'un véhicule
    @GetMapping("/api/proprio/vehicule/{id}")
    public ProprietaireContactDTO getContactVehicule(@PathVariable("id") Long vehiculeId) {
        return proprietaireService.getProprietaireContactVehicule(vehiculeId);
    }   
    
}
