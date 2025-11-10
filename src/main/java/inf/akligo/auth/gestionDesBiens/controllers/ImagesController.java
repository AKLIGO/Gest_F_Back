package inf.akligo.auth.gestionDesBiens.controllers;
import inf.akligo.auth.gestionDesBiens.requests.ImageDtoApp;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTO;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTOVeh;
import inf.akligo.auth.gestionDesBiens.entity.Images;
import inf.akligo.auth.gestionDesBiens.services.serviceImage.ServiceImage;
import org.springframework.http.ResponseEntity;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaTypeFactory;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImagesController{

    private final ServiceImage serviceImage;

    /**
     * Uploader une image et la lier à un appartement
     */
    @PostMapping("/ajoutImageApp")
    public ResponseEntity<?> uploadImageWithAppartement(
            @RequestParam("libelle") String libelle,
            @RequestParam("file") MultipartFile file,
            @RequestParam("appartementId") Long appartementId) {
        try {
            Images image = serviceImage.uploadImageWithAppartement(libelle, file, appartementId);
            return ResponseEntity.ok(image);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'upload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/ajoutImaAppNom")
    public ResponseEntity<?> uploadImageWithAppartementByName(
            @RequestParam("libelle") String libelle,
            @RequestParam("file") MultipartFile file,
            @RequestParam("appartementNom") String appartementNom
    ){
        try{
            Images image =serviceImage.uploadImageWithAppartementByName(libelle, file, appartementNom);
            return ResponseEntity.ok(image);
        }catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'upload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    @PostMapping("/ajoutImageTovehicule")
    public ResponseEntity<?> uploadImageWithVehiculeByImmatriculation(
            @RequestParam("libelle") String libelle,
            @RequestParam("file") MultipartFile file,
            @RequestParam("immatriculationVehicule") String immatriculationVehicule
    )
    {
        try{
            Images image = serviceImage.uploadImageWithVehiculeByImmatriculation(libelle, file, immatriculationVehicule);
            return ResponseEntity.ok(image);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'upload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Retourne juste les infos en base 
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        try {
            Images image = serviceImage.getImageById(id);
            return ResponseEntity.ok(new ImageDtoApp(image.getId(),
                                    image.getLibelle(),
                                    image.getNomFichier(),
                                    image.getTypeMime(),
                                    image.getAppartement() != null ? image.getAppartement().getId() : null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint pour servir le fichier physique
     */

    @GetMapping("file/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename){
        try {
            
            Resource resource = serviceImage.loadFileAsResource(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }



    /**
     * Endpoint pour uploader une image libre
     * POST /api/images/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Images> uploadImageLibre(
            @RequestParam("libelle") String libelle,
            @RequestParam("file") MultipartFile file) {

        try {
            Images savedImage = serviceImage.uploadImageLibre(libelle, file);
            return new ResponseEntity<>(savedImage, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint pour récupérer toutes les images libres
     * GET /api/images/libres
     */
    @GetMapping("/libres")
    public ResponseEntity<List<Images>> getAllImagesLibres() {
        List<Images> imagesLibres = serviceImage.getAllImagesLibres();
        if (imagesLibres.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(imagesLibres, HttpStatus.OK);
    }

    @GetMapping("/vehicules")
    public ResponseEntity<List<ImageDTOVeh>> listImagesVehicules() {
        return ResponseEntity.ok(serviceImage.getImagesVehicules());
    }

    @GetMapping("/appartements")
    public ResponseEntity<List<ImageDtoApp>> listImagesAppartements() {
        return ResponseEntity.ok(serviceImage.getImagesAppartements());
    }


    @GetMapping("/files/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        try {
            // Extraire le chemin réel après /file/
            String path = request.getRequestURI().substring("/api/image/file/".length());
            Resource resource = serviceImage.loadFileAsResource(path);

            // Déduire le type MIME
            String mimeType = Files.probeContentType(Paths.get("uploads/images/" + Paths.get(path).getFileName()));
            if (mimeType == null) mimeType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
         } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------- VEHICULE -------------------

    @PutMapping("/vehicule/{id}")
    public ResponseEntity<ImageDTOVeh> updateVehiculeImage(
        @PathVariable Long id,
        @RequestBody ImageDTOVeh dto
    ){
        ImageDTOVeh updated = serviceImage.updateImage(id,dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/vehicule/{id}")
    public ResponseEntity<Void> deleteVehiculeImage(@PathVariable Long id){

        serviceImage.deleteImage(id);

        return ResponseEntity.noContent().build();

    }

    // ------------------- APPARTEMENT -------------------

    @PutMapping("/appartement/{id}")
    public ResponseEntity<ImageDtoApp> updateAppartementImage(
            @PathVariable Long id,
            @RequestBody ImageDtoApp dto
    ) {
        ImageDtoApp updated = serviceImage.updateImageApp(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/appartement/{id}")
    public ResponseEntity<Void> deleteAppartementImage(@PathVariable Long id) {
        serviceImage.deleteImageApp(id);
        return ResponseEntity.noContent().build();
    }

      // 1️⃣ Récupérer les images de l'utilisateur connecté
    @GetMapping("/me/appartements")
    public ResponseEntity<List<ImageDtoApp>> getImagesAppartementsByConnectedUser(Authentication authentication) {
        String email = authentication.getName();
        List<ImageDtoApp> images = serviceImage.getImagesAppartementsByConnectedUser(email);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/me/vehicules")
    public ResponseEntity<List<ImageDTOVeh>> getImagesVehiculesByConnectedUser(Authentication authentication) {
        String email = authentication.getName();
        List<ImageDTOVeh> images = serviceImage.getImagesVehiculesByConnectedUser(email);
        return ResponseEntity.ok(images);
    }

        // 2️⃣ Récupérer les images d’un propriétaire spécifique
    @GetMapping("/appartements/{proprietaireId}")
    public ResponseEntity<List<ImageDtoApp>> getImagesAppartementsByProprietaire(@PathVariable Long proprietaireId) {
        List<ImageDtoApp> images = serviceImage.getImagesAppartementsByProprietaire(proprietaireId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/vehicules/{proprietaireId}")
    public ResponseEntity<List<ImageDTOVeh>> getImagesVehiculesByProprietaire(@PathVariable Long proprietaireId) {
        List<ImageDTOVeh> images = serviceImage.getImagesVehiculesByProprietaire(proprietaireId);
        return ResponseEntity.ok(images);
    }



}