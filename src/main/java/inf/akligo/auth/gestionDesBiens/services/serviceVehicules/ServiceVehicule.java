package inf.akligo.auth.gestionDesBiens.services.serviceVehicules;
import inf.akligo.auth.gestionDesBiens.requests.VehiculeDTO;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
//import inf.akligo.auth.gestionDesBiens.entity.Immeuble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface ServiceVehicule{

    public Vehicules addVehicules(Vehicules Vehicules);
    public Vehicules updateVehicules(Vehicules VehiculesUpd,Long id);
    public void removeVehicules(Long id);
    public Vehicules changerStatutVehy(String nvStatut,Long id);
    List<Vehicules> listVehicules();

    public boolean isDiponible(Long VehiculesId);
    public Vehicules getVehiculesById(Long id);
    List<VehiculeDTO> getAllVehiculesDTO();
    
    // Nouvelles méthodes pour le filtrage par propriétaire
    List<VehiculeDTO> getVehiculesByProprietaire(Long proprietaireId);
    List<VehiculeDTO> getVehiculesByCurrentUser();

}