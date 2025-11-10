package inf.akligo.auth.gestionDesBiens.services.serviceApp;

import org.springframework.transaction.annotation.Transactional;

import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.requests.ImageDtoApp;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTO;
import inf.akligo.auth.gestionDesBiens.repository.AppartementRepository;
import org.springframework.stereotype.Service;
import inf.akligo.auth.gestionDesBiens.services.serviceApp.ServiceApp;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutAppartement;
import inf.akligo.auth.gestionDesBiens.requests.AppartementDTO;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

@Service

public class ServiceAppImpl implements ServiceApp{
    private final AppartementRepository appartementRepository;
    private final UtilisateurRepository utilisateurRepository;


    private static final Logger log = LoggerFactory.getLogger(ServiceAppImpl.class);
    public ServiceAppImpl(AppartementRepository appartementRepository, UtilisateurRepository utilisateurRepository) {
        this.appartementRepository = appartementRepository;
        this.utilisateurRepository= utilisateurRepository;
    }


    @Transactional
    @Override
    public Appartement addAppartement(Appartement appartement){

        // 🔐 Récupérer l'utilisateur connecté via le token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 👤 Récupérer l'utilisateur dans la base
        Utilisateurs proprietaire = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

          // 🏠 Associer le propriétaire
        appartement.setProprietaire(proprietaire);
        Appartement savedAppartement = appartementRepository.save(appartement);
        return savedAppartement;
    }

    @Override
    public Appartement updateAppartement(Appartement appartementUpd, Long id){
        Optional<Appartement> optAppartement = appartementRepository.findById(id);
        if (optAppartement.isPresent()){
            Appartement appartement=optAppartement.get();
            appartement.setNom(appartementUpd.getNom());
            appartement.setNumero(appartementUpd.getNumero());
            appartement.setSuperficie(appartementUpd.getSuperficie());
            appartement.setAdresse(appartementUpd.getAdresse());
            appartement.setNbrDePieces(appartementUpd.getNbrDePieces());
            appartement.setDescription(appartementUpd.getDescription());
            appartement.setStatut(appartementUpd.getStatut());
            appartement.setLocalisation(appartementUpd.getLocalisation());
            appartement.setType(appartementUpd.getType());

            return appartementRepository.save(appartement);

        }
        else {
            throw new RuntimeException("Appartement avec l'id " + id + " non trouvé");
        }
    }

    @Override
    public void removeAppartement(Long id) {
        appartementRepository.deleteById(id);
    }



    @Override
    public List<Appartement> listAppartement() {
        return appartementRepository.findAll();
    }

    @Override
    public Appartement changerStatutApp(String nvStatut,Long id){
        //recuperer l'appartement dans la Base
        Appartement optApp = appartementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appartement nn trouver"));

         // Convertir String en enum
        StatutAppartement nvStatutConv = StatutAppartement.valueOf(nvStatut);

        // affecter une nouvelle valeur (Statut) a l'appartement
        
        optApp.setStatut(nvStatutConv);

        // enregistrer l'appartement apres avoir effectuer la modification
        return appartementRepository.save(optApp);
    }

    @Override
    public boolean isDiponible(Long appartementId){

        //recuperer l'appartement
        // Appartement appDisp= appartementRepository.findById(appartementId);

        // retourner vrai si le statut de l'appartement rechercher est dispo ; false dans le cas contraire

        return appartementRepository.findByIdAndStatut(appartementId,StatutAppartement.DISPONIBLE)
                    .isPresent();

    };


    
    @Override
    public Appartement getAppartementById(Long id){
        // recuperer un utilisateur a partir de son identifiant
        return appartementRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("appartement non trouver"));
    }


@Override
public List<AppartementDTO> getAllAppartementsDTO() {
    List<Appartement> appartements = appartementRepository.findByPublieTrue();
    System.out.println("Nombre d'appartements récupérés : " + appartements.size());
    if (appartements == null) {
        appartements = new ArrayList<>();
    }

    return appartements.stream()
            .map(appartement -> {
                try {
                    return convertToDTO(appartement);
                } catch (Exception e) {
                    e.printStackTrace(); // log l'erreur pour chaque appartement
                    return null; // ignore l'appartement qui pose problème
                }
            })
            .filter(Objects::nonNull) // retire les DTO nuls
            .collect(Collectors.toList());
}

/**
 * Récupère tous les appartements d'un propriétaire spécifique
 * @param proprietaireId L'ID du propriétaire
 * @return Liste des appartements du propriétaire
 */
@Override
public List<AppartementDTO> getAppartementsByProprietaire(Long proprietaireId) {
    log.info("Récupération des appartements pour le propriétaire ID: {}", proprietaireId);
    
    List<Appartement> appartements = appartementRepository.findByProprietaireId(proprietaireId);
    log.info("Nombre d'appartements trouvés pour le propriétaire {}: {}", proprietaireId, appartements.size());
    
    if (appartements == null) {
        appartements = new ArrayList<>();
    }

    return appartements.stream()
            .map(appartement -> {
                try {
                    return convertToDTO(appartement);
                } catch (Exception e) {
                    log.error("Erreur lors de la conversion de l'appartement ID {}: {}", appartement.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
}

/**
 * Récupère les appartements du propriétaire connecté
 * @return Liste des appartements du propriétaire connecté
 */
@Override
public List<AppartementDTO> getAppartementsByCurrentUser() {
    // 🔐 Récupérer l'utilisateur connecté via le token
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    log.info("Récupération des appartements pour l'utilisateur connecté: {}", username);

    // 👤 Récupérer l'utilisateur dans la base
    Utilisateurs proprietaire = utilisateurRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    return getAppartementsByProprietaire(proprietaire.getId());
}



public AppartementDTO convertToDTO(Appartement appartement) {
    List<ImageDTO> imageDto = new ArrayList<>();

    if (appartement.getImages() != null && !appartement.getImages().isEmpty()) {
        imageDto = appartement.getImages().stream()
                .map(image -> ImageDTO.builder()
                        .id(image.getId())
                        .libelle(image.getLibelle())
                        .nomFichier(image.getNomFichier())
                        .typeMime(image.getTypeMime())
                        .appartementId(appartement.getId())
                        .previewUrl("http://localhost:8082/api/image/file/" + image.getNomFichier())
                        .build())
                .collect(Collectors.toList());
    }

    return AppartementDTO.builder()
            .id(appartement.getId())
            .nom(appartement.getNom())
            .adresse(appartement.getAdresse())
            .numero(appartement.getNumero())
            .superficie(appartement.getSuperficie())
            .nbrDePieces(appartement.getNbrDePieces())
            .description(appartement.getDescription())
            .prix(appartement.getPrix())
            .type(appartement.getType())
            .statut(appartement.getStatut())
            .createdAt(appartement.getCreatedAt())
            .localisation(appartement.getLocalisation())
            .lastModifiedDate(appartement.getLastModifiedDate())
            .images(imageDto)
            .build();
}

@Override
public Appartement autoriserAffichage(Long id, boolean publie) {
        Appartement appartement = appartementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appartement non trouvé"));
        appartement.setPublie(publie);
        return appartementRepository.save(appartement);
    }
   

}