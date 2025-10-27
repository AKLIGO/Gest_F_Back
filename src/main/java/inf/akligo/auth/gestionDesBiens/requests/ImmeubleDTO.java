package inf.akligo.auth.gestionDesBiens.requests;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeImmeuble;
import java.time.LocalDateTime;
import inf.akligo.auth.gestionDesBiens.requests.ImageDtoApp;
import java.time.LocalDateTime; // Pour LocalDateTime
import java.util.List;
public class ImmeubleDTO{
    private Long id;
    private String nom;
    private String ville;
    private TypeImmeuble type;
    private String description;
    private int nbrAppartment;
    private int nbrEtage;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedDate;

        // Constructeur complet
    public ImmeubleDTO(Long id, String nom, String ville, TypeImmeuble type,
                       String description, int nbrAppartment, int nbrEtage,
                       LocalDateTime createdAt, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.nom = nom;
        this.ville = ville;
        this.type = type;
        this.description = description;
        this.nbrAppartment = nbrAppartment;
        this.nbrEtage = nbrEtage;
        this.createdAt = createdAt;
        this.lastModifiedDate = lastModifiedDate;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public TypeImmeuble getType() { return type; }
    public void setType(TypeImmeuble type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getNbrAppartment() { return nbrAppartment; }
    public void setNbrAppartment(int nbrAppartment) { this.nbrAppartment = nbrAppartment; }

    public int getNbrEtage() { return nbrEtage; }
    public void setNbrEtage(int nbrEtage) { this.nbrEtage = nbrEtage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
}