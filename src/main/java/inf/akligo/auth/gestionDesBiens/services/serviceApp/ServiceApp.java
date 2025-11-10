package inf.akligo.auth.gestionDesBiens.services.serviceApp;

import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.entity.Immeuble;
import inf.akligo.auth.gestionDesBiens.requests.AppartementDTO;


import java.util.List;


public interface ServiceApp{

    public Appartement addAppartement(Appartement appartement);
    public Appartement updateAppartement(Appartement appartementUpd,Long id);
    public void removeAppartement(Long id);
    public Appartement changerStatutApp(String nvStatut,Long id);
    List<Appartement> listAppartement();
    List<AppartementDTO> getAllAppartementsDTO();
    public boolean isDiponible(Long appartementId);
    public Appartement getAppartementById(Long id);

    Appartement autoriserAffichage(Long id, boolean publie);
    
    // Nouvelles méthodes pour le filtrage par propriétaire
    List<AppartementDTO> getAppartementsByProprietaire(Long proprietaireId);
    List<AppartementDTO> getAppartementsByCurrentUser();


}