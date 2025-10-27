package inf.akligo.auth.gestionDesBiens.requests;

import java.time.LocalDateTime;
import java.util.List;
import inf.akligo.auth.gestionDesBiens.requests.ImageDTO;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeAppartement; 
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutAppartement; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppartementDTO {
    
    private Long id;
    private String nom;
    private String adresse;
    private int numero;
    private String superficie;
    private int nbrDePieces;
    private String description;
    private double prix;
    private TypeAppartement type; // Utiliser l'enum
    private StatutAppartement statut; // Utiliser l'enum
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedDate;
    private List<ImageDTO> images; // Liste des images

    // Vous pouvez ajouter d'autres méthodes si nécessaire
}