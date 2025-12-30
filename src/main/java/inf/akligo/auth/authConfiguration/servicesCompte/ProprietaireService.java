package inf.akligo.auth.authConfiguration.servicesCompte;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.datas.ProprietaireContactDTO;
import inf.akligo.auth.gestionDesBiens.repository.AppartementRepository;
import inf.akligo.auth.gestionDesBiens.repository.VehiculeRepository;

@Service
public class ProprietaireService {
    private final AppartementRepository appartementRepository;
    private final VehiculeRepository vehiculeRepository;

    public ProprietaireService(AppartementRepository appartementRepository,
                               VehiculeRepository vehiculeRepository) {
        this.appartementRepository = appartementRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    @Transactional(readOnly = true)
    public ProprietaireContactDTO getProprietaireContactAppartement(Long appartementId) {
        return appartementRepository.findById(appartementId)
                .map(app -> {
                    Utilisateurs prop = app.getProprietaire();
                    if (prop == null) {
                        return ProprietaireContactDTO.builder()
                                .nom("Propriétaire non disponible")
                                .build();
                    }
                    return ProprietaireContactDTO.builder()
                            .nom(prop.getFullName())
                            .email(prop.getEmail())
                            .telephone(prop.getTelephone())
                            .mailtoLink("mailto:" + prop.getEmail())
                            .build();
                })
                .orElse(ProprietaireContactDTO.builder()
                        .nom("Appartement non trouvé")
                        .build());
    }

    @Transactional(readOnly = true)
    public ProprietaireContactDTO getProprietaireContactVehicule(Long vehiculeId) {
        return vehiculeRepository.findById(vehiculeId)
                .map(veh -> {
                    Utilisateurs prop = veh.getProprietaire();
                    if (prop == null) {
                        return ProprietaireContactDTO.builder()
                                .nom("Propriétaire non disponible")
                                .build();
                    }
                    return ProprietaireContactDTO.builder()
                            .nom(prop.getFullName())
                            .email(prop.getEmail())
                            .telephone(prop.getTelephone())
                            .mailtoLink("mailto:" + prop.getEmail())
                            .build();
                })
                .orElse(ProprietaireContactDTO.builder()
                        .nom("Véhicule non trouvé")
                        .build());
    }   
}
