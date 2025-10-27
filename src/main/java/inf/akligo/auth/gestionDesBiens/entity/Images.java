package inf.akligo.auth.gestionDesBiens.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeAppartement;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutAppartement;
import inf.akligo.auth.gestionDesBiens.entity.Immeuble;
import lombok.ToString;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany; 
import com.fasterxml.jackson.annotation.JsonBackReference; 
import lombok.EqualsAndHashCode;
import jakarta.persistence.Lob;


@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Images{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier; // ex: "173123_photo.png"

    @Column(name = "type_mime")
    private String typeMime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appartement_id", nullable = true)
    @ToString.Exclude
    @JsonBackReference("appartement-images")
    private Appartement appartement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = true)
    @ToString.Exclude
    //@JsonBackReference("vehicule-images")
    private Vehicules vehicule;

    


    
}