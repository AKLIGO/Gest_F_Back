package inf.akligo.auth.gestionDesBiens.requests;

import inf.akligo.auth.gestionDesBiens.enumerateurs.Carburant;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutVehicule;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeVehicule;
import inf.akligo.auth.gestionDesBiens.entity.Images;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTOVeh;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeDTO {

    private Long id;

    private String marque;
    private String modele;
    private String immatriculation;
    private double prix;
    private Carburant carburant;
    private StatutVehicule statut;
    private TypeVehicule type;

    // Liste des images associées au véhicule
    private List<ImageDTOVeh> images;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedDate;
}
