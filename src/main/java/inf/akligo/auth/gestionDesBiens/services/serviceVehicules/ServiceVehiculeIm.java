package inf.akligo.auth.gestionDesBiens.services.serviceVehicules;


import org.springframework.transaction.annotation.Transactional;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import inf.akligo.auth.gestionDesBiens.repository.VehiculeRepository;
import inf.akligo.auth.gestionDesBiens.requests.VehiculeDTO;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTOVeh;
import org.springframework.stereotype.Service;
import inf.akligo.auth.authConfiguration.repository.RoleRepository;
import inf.akligo.auth.gestionDesBiens.services.serviceVehicules.ServiceVehicule;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutVehicule;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeVehicule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceVehiculeIm implements ServiceVehicule{

    private final VehiculeRepository vehiculeRepository;
    private final UtilisateurRepository utilisateurRepository;
    
    @Override
    @Transactional
    public Vehicules addVehicules(Vehicules vehicules){
        
        // 🔐 Récupérer l'utilisateur connecté via le token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 👤 Récupérer l'utilisateur dans la base
        Utilisateurs proprietaire = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 🏠 Associer le propriétaire

        vehicules.setProprietaire(proprietaire);
        Vehicules saveVehicule = vehiculeRepository.save(vehicules);
        return saveVehicule;
    }
    @Override
    public Vehicules updateVehicules(Vehicules vehiculesUpd,Long id){

        // 1. Récupérer le véhicule existant
    Optional<Vehicules> optionalVehicule = vehiculeRepository.findById(id);
    
    if (optionalVehicule.isPresent()) {
        Vehicules vehicule = optionalVehicule.get();

        // 2. Mettre à jour les champs
        if (vehiculesUpd.getMarque() != null) vehicule.setMarque(vehiculesUpd.getMarque());
        if (vehiculesUpd.getModele() != null) vehicule.setModele(vehiculesUpd.getModele());
        if (vehiculesUpd.getImmatriculation() != null) vehicule.setImmatriculation(vehiculesUpd.getImmatriculation());
        if (vehiculesUpd.getPrix() != 0) vehicule.setPrix(vehiculesUpd.getPrix());
        if (vehiculesUpd.getCarburant() != null) vehicule.setCarburant(vehiculesUpd.getCarburant());
        if (vehiculesUpd.getStatut() != null) vehicule.setStatut(vehiculesUpd.getStatut());
        if (vehiculesUpd.getType() != null) vehicule.setType(vehiculesUpd.getType());
        // Pour les images, il faudra gérer la mise à jour séparément si nécessaire

        // 3. Sauvegarder le véhicule
        return vehiculeRepository.save(vehicule);
    } else {
        // Si le véhicule n'existe pas, retourner null ou lancer une exception
        return null;
    }

       
    }

    @Override
    public void removeVehicules(Long id){

        vehiculeRepository.deleteById(id);

    }

    @Override
    public Vehicules changerStatutVehy(String nvStatut,Long id){
         Optional<Vehicules> v = vehiculeRepository.findById(id);
        if (v.isPresent()) {
            // Conversion de String vers Enum
            try {
                StatutVehicule statutEnum = StatutVehicule.valueOf(nvStatut.toUpperCase());
                v.get().setStatut(statutEnum);
                return vehiculeRepository.save(v.get());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide : " + nvStatut);
             }
        }
        return null;
     }
    @Override
    public List<Vehicules> listVehicules(){

        return vehiculeRepository.findAll();
    }
    @Override
    public boolean isDiponible(Long vehiculesId){
        return vehiculeRepository.findById(vehiculesId)
            .map(v -> v.getStatut() == StatutVehicule.DISPONIBLE)
            .orElse(false);
    }
    @Override
    public Vehicules getVehiculesById(Long id){

        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicule non trouvé avec id: " + id));

    }

     @Override
     @Transactional(readOnly = true)
     public List<VehiculeDTO> getAllVehiculesDTO() {
        List<Vehicules> vehicules = vehiculeRepository.findByPublieTrue();
        System.out.println("Nombre de véhicules récupérés : " + vehicules.size());
    
        if (vehicules == null) {
            vehicules = new ArrayList<>();
        }

        return vehicules.stream()
                .map(vehicule -> {
                    try {
                        return convertToDTO(vehicule);
                    } catch (Exception e) {
                        e.printStackTrace(); // log l’erreur pour chaque véhicule
                        return null; // ignore le véhicule qui pose problème
                    }
                })
                .filter(Objects::nonNull) // retire les DTO nuls
                .collect(Collectors.toList());
        }


    //convertisseur


    public VehiculeDTO convertToDTO(Vehicules vehicule) {
    List<ImageDTOVeh> imageDtos = new ArrayList<>();

    if (vehicule.getImages() != null && !vehicule.getImages().isEmpty()) {
        imageDtos = vehicule.getImages().stream()
                .map(image -> ImageDTOVeh.builder()
                        .id(image.getId())
                        .libelle(image.getLibelle())
                        .nomFichier(image.getNomFichier())
                        .typeMime(image.getTypeMime())
                        .vehiculeId(vehicule.getId())
                        .previewUrl("http://localhost:8082/api/image/file/" + image.getNomFichier())
                        .build())
                .collect(Collectors.toList());
    }

    return VehiculeDTO.builder()
            .id(vehicule.getId())
            .marque(vehicule.getMarque())
            .modele(vehicule.getModele())
            .immatriculation(vehicule.getImmatriculation())
            .prix(vehicule.getPrix())
            .carburant(vehicule.getCarburant())
            .statut(vehicule.getStatut())
            .type(vehicule.getType())
            .images(imageDtos)
            .createdAt(vehicule.getCreatedAt())
            .lastModifiedDate(vehicule.getLastModifiedDate())
            .build();
}

/**
 * Récupère tous les véhicules d'un propriétaire spécifique
 * @param proprietaireId L'ID du propriétaire
 * @return Liste des véhicules du propriétaire
 */
@Transactional
@Override
public List<VehiculeDTO> getVehiculesByProprietaire(Long proprietaireId) {
    System.out.println("Récupération des véhicules pour le propriétaire ID: " + proprietaireId);
    
    List<Vehicules> vehicules = vehiculeRepository.findByProprietaireId(proprietaireId);
    System.out.println("Nombre de véhicules trouvés pour le propriétaire " + proprietaireId + ": " + vehicules.size());
    
    if (vehicules == null) {
        vehicules = new ArrayList<>();
    }

    return vehicules.stream()
            .map(vehicule -> {
                try {
                    return convertToDTO(vehicule);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la conversion du véhicule ID " + vehicule.getId() + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
}

/**
 * Récupère les véhicules du propriétaire connecté
 * @return Liste des véhicules du propriétaire connecté
 */
@Override
public List<VehiculeDTO> getVehiculesByCurrentUser() {
    // 🔐 Récupérer l'utilisateur connecté via le token
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    System.out.println("Récupération des véhicules pour l'utilisateur connecté: " + username);

    // 👤 Récupérer l'utilisateur dans la base
    Utilisateurs proprietaire = utilisateurRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    return getVehiculesByProprietaire(proprietaire.getId());
}

    @Override
    public Vehicules autoriserAffichage(Long id, boolean publie) {
            Vehicules vehicule = vehiculeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            vehicule.setPublie(publie);
            return vehiculeRepository.save(vehicule);
        }  

    @Override
    public List<VehiculeDTO> rechercherVehicules(
            String marque,
            Double prixMin,
            Double prixMax
    ) {

        // Valeurs par défaut
        String marqueRecherche = (marque == null) ? "" : marque;
        double min = (prixMin == null) ? 0 : prixMin;
        double max = (prixMax == null) ? Double.MAX_VALUE : prixMax;

        List<Vehicules> vehicules =
                vehiculeRepository.findByPublieTrueAndMarqueContainingIgnoreCaseAndPrixBetween(
                        marqueRecherche, min, max
                );

        return vehicules.stream()
                .map(this::convertToDTO)
                .toList();
    }



}