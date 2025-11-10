package inf.akligo.auth.gestionDesBiens.services.serviceImage;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.gestionDesBiens.entity.Images;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import inf.akligo.auth.gestionDesBiens.requests.ImageDtoApp;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTOVeh;

import inf.akligo.auth.gestionDesBiens.repository.ImageRepository;
import inf.akligo.auth.gestionDesBiens.repository.VehiculeRepository;
import inf.akligo.auth.gestionDesBiens.repository.AppartementRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;


@RequiredArgsConstructor
@Service
public class ServiceImage {

    
    private final ImageRepository imagesRepository;
    private final AppartementRepository appartementsRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UtilisateurRepository utilisateurRepository;

    private static final String UPLOAD_DIR = "uploads/images/";

    public Images uploadImageWithAppartement(String libelle, MultipartFile file, Long appartementId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Appartement appartement = appartementsRepository.findById(appartementId)
                .orElseThrow(() -> new IllegalArgumentException("Appartement not found with id " + appartementId));

        // Vérifier le dossier
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Générer un nom unique
        String sanitizeFileName =sanitizeFileName(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "_" + sanitizeFileName;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Sauvegarde physique
        Files.copy(file.getInputStream(), filePath);

        // Sauvegarde en base
        Images image = Images.builder()
                .libelle(libelle)
                .nomFichier(fileName)
                .typeMime(file.getContentType())
                .appartement(appartement)
                .build();

        return imagesRepository.save(image);
    }

    public Images uploadImageWithAppartementByName(String libelle, MultipartFile file, String nomDeAppartement) throws IOException {
        Appartement appartement = appartementsRepository.findByNom(nomDeAppartement)
                .orElseThrow(() -> new IllegalArgumentException("Appartement introuvable avec le nom " + nomDeAppartement));

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String sanitizeFileName = sanitizeFileName(file.getOriginalFilename());

        String fileName = System.currentTimeMillis() + "_" + sanitizeFileName;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Sauvegarde physique
        Files.copy(file.getInputStream(), filePath);

        // Sauvegarde en base
        Images image = Images.builder()
                .libelle(libelle)
                .nomFichier(fileName) // ✅ corrigé
                .typeMime(file.getContentType())
                .appartement(appartement)
                .build();

        return imagesRepository.save(image);
    }


    public Images uploadImageWithVehiculeByImmatriculation(String libelle, MultipartFile file, String immatriculationVehicule) throws IOException {

       Optional<Vehicules> optionalVehicule = vehiculeRepository.findByImmatriculation(immatriculationVehicule);
      if (optionalVehicule.isEmpty()) {
                throw new IllegalArgumentException("Vehicule introuvable avec immatriculation : " + immatriculationVehicule);
                
            }
        Vehicules vehicule = optionalVehicule.get();
        System.out.println("Vehicule trouvé : " + vehicule.getMarque());
        
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String sanitizeFileName = sanitizeFileName(file.getOriginalFilename());

        String fileName = System.currentTimeMillis() + "_" + sanitizeFileName;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Sauvegarde physique
        Files.copy(file.getInputStream(), filePath);

        // Sauvegarde en base
        Images image = Images.builder()
                .libelle(libelle)
                .nomFichier(fileName) 
                .typeMime(file.getContentType())
                .vehicule(vehicule)
                .build();

        return imagesRepository.save(image);


    }

    public Images getImageById(Long id) {
        return imagesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image introuvable avec l'id " + id));

                
    }




    // Récupération du fichier physique
    public Resource loadFileAsResource(String fileName) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new IOException("Fichier introuvable: " + fileName);
        }

        return resource;
    }

    /**
     * Methode pour netoyer le nom de fichier
     */

    public String sanitizeFileName(String original){

        return original
                .replaceAll("[\\s]","_")    // remplace les espaces par _
                .replaceAll("[^a-zA-Z0-9_.-]",""); // supprime tous les caractères spéciaux sauf _ . -
    }


    public Images uploadImageLibre(String libelle, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String sanitizeFileName = sanitizeFileName(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "_" + sanitizeFileName;
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        Files.copy(file.getInputStream(), filePath);

        Images image = Images.builder()
                .libelle(libelle)
                .nomFichier(fileName)
                .typeMime(file.getContentType())
                .build();

        return imagesRepository.save(image);
    }


    /**
     * Récupérer toutes les images libres
     */
    public List<Images> getAllImagesLibres() {
        return imagesRepository.findByAppartementIsNullAndVehiculeIsNull();
    }

   


   
    public List<ImageDTOVeh> getImagesVehicules() {
        return imagesRepository.findByVehiculeIsNotNull().stream()
                .map(this::convertToVehiculeDTO)
                .collect(Collectors.toList());
    }

    public List<ImageDtoApp> getImagesAppartements() {
        return imagesRepository.findByAppartementIsNotNull().stream()
                .map(this::convertToAppDTO)
                .collect(Collectors.toList());
    }


    private ImageDTOVeh convertToVehiculeDTO(Images img) {
    return ImageDTOVeh.builder()
            .id(img.getId())
            .libelle(img.getLibelle())
            .nomFichier(img.getNomFichier())
            .typeMime(img.getTypeMime())
            .vehiculeId(img.getVehicule().getId())
            .previewUrl("/uploads/images/" + img.getNomFichier()) // chemin local pour le moment
            .build();
}

private ImageDtoApp convertToAppDTO(Images img) {
    return ImageDtoApp.builder()
            .id(img.getId())
            .libelle(img.getLibelle())
            .nomFichier(img.getNomFichier())
            .typeMime(img.getTypeMime())
            .appartementId(img.getAppartement().getId())
            .build();
}

/**
 * updates
 */

    public ImageDTOVeh updateImage(Long id, ImageDTOVeh dto) {
        Images image = imagesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image non trouvée avec id: " + id));

        if (dto.getLibelle() != null) image.setLibelle(dto.getLibelle());
        if (dto.getNomFichier() != null) image.setNomFichier(dto.getNomFichier());

        if (dto.getVehiculeId() != null) {
            Vehicules vehicule = vehiculeRepository.findById(dto.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec id: " + dto.getVehiculeId()));
            image.setVehicule(vehicule);
        }

        Images updated = imagesRepository.save(image);

        return ImageDTOVeh.builder()
                .id(updated.getId())
                .libelle(updated.getLibelle())
                .nomFichier(updated.getNomFichier())
                .typeMime(updated.getTypeMime())
                .vehiculeId(updated.getVehicule() != null ? updated.getVehicule().getId() : null)
                .previewUrl("/api/image/file/" + updated.getNomFichier())
                .build();
        }
    //Delete image
    public void deleteImage(Long id) {
        if (!imagesRepository.existsById(id)) {
            throw new RuntimeException("Image non trouvée avec id: " + id);
        }
        imagesRepository.deleteById(id);
    }



    // Update image pour un appartement
    public ImageDtoApp updateImageApp(Long id, ImageDtoApp dto) {
        Images image = imagesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image non trouvée avec id: " + id));

        // Mise à jour des champs simples
        if (dto.getLibelle() != null) image.setLibelle(dto.getLibelle());
        if (dto.getNomFichier() != null) image.setNomFichier(dto.getNomFichier());

        // Mise à jour de la relation avec l'appartement
        if (dto.getAppartementId() != null) {
            Appartement appartement = appartementsRepository.findById(dto.getAppartementId())
                    .orElseThrow(() -> new RuntimeException("Appartement non trouvé avec id: " + dto.getAppartementId()));
            image.setAppartement(appartement);
        }

        Images updated = imagesRepository.save(image);

        // Retour du DTO mis à jour
        return ImageDtoApp.builder()
                .id(updated.getId())
                .libelle(updated.getLibelle())
                .nomFichier(updated.getNomFichier())
                .typeMime(updated.getTypeMime())
                .appartementId(updated.getAppartement() != null ? updated.getAppartement().getId() : null)
                .build();
    }

    // Delete image pour un appartement
    public void deleteImageApp(Long id) {
        if (!imagesRepository.existsById(id)) {
            throw new RuntimeException("Image non trouvée avec id: " + id);
        }
        imagesRepository.deleteById(id);
    }

    // 1️⃣ Récupérer les images des appartements du propriétaire connecté

     public List<ImageDtoApp> getImagesAppartementsByConnectedUser(String email) {
        Utilisateurs proprietaire = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return imagesRepository.findByAppartementProprietaire(proprietaire).stream()
                .map(this::convertToAppDTO)
                .collect(Collectors.toList());
    }


    // 1️⃣ Récupérer les images des véhicules du propriétaire connecté

    public List<ImageDTOVeh> getImagesVehiculesByConnectedUser(String email) {
        Utilisateurs proprietaire = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return imagesRepository.findByVehiculeProprietaire(proprietaire).stream()
                .map(this::convertToVehiculeDTO)
                .collect(Collectors.toList());
    }


    // 2️⃣ Récupérer les images par propriétaire spécifique

       public List<ImageDtoApp> getImagesAppartementsByProprietaire(Long proprietaireId) {
        Utilisateurs proprietaire = utilisateurRepository.findById(proprietaireId)
                .orElseThrow(() -> new RuntimeException("Propriétaire non trouvé"));

        return imagesRepository.findByAppartementProprietaire(proprietaire).stream()
                .map(this::convertToAppDTO)
                .collect(Collectors.toList());
    }

    
    public List<ImageDTOVeh> getImagesVehiculesByProprietaire(Long proprietaireId) {
        Utilisateurs proprietaire = utilisateurRepository.findById(proprietaireId)
                .orElseThrow(() -> new RuntimeException("Propriétaire non trouvé"));

        return imagesRepository.findByVehiculeProprietaire(proprietaire).stream()
                .map(this::convertToVehiculeDTO)
                .collect(Collectors.toList());
    }
    }
