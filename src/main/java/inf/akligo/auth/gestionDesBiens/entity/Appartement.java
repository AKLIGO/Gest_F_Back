package inf.akligo.auth.gestionDesBiens.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeAppartement;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutAppartement;
import inf.akligo.auth.gestionDesBiens.entity.Immeuble;
import lombok.ToString;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Appartement{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String adresse;
    private int numero;
    private String superficie;
    private int nbrDePieces;
    private String description;
    private double prix;
    private String localisation;

    @Builder.Default
    private boolean publie = false;

    @Enumerated(EnumType.STRING)
    private TypeAppartement type;

    @Enumerated(EnumType.STRING)
    private StatutAppartement statut;

    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;


    @ManyToOne
    @JoinColumn(name="immeuble_id")
    @JsonBackReference("immeuble-appartement")
    @ToString.Exclude
    private Immeuble immeuble;

    @OneToMany(mappedBy = "appartement", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @JsonManagedReference("appartement-images")
    @ToString.Exclude
    //@JsonManagedReference
    @JsonIgnore
    private List<Images> images = new ArrayList<>();

    /**
     * modification apporter le 21-10-2025
     */


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="proprietaire_id")
    @JsonBackReference("proprietaire-appartement")
    private Utilisateurs proprietaire;
}
