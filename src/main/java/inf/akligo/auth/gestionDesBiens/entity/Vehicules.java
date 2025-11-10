package inf.akligo.auth.gestionDesBiens.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeVehicule;
import inf.akligo.auth.gestionDesBiens.enumerateurs.Carburant;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutVehicule;
import inf.akligo.auth.gestionDesBiens.entity.Images;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Vehicules{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marque;
    private String modele;
    private String immatriculation;
    private double prix;
    private Carburant carburant;
    private StatutVehicule statut;
    private TypeVehicule type; 

    @Builder.Default
    private boolean publie = false;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true,fetch =FetchType.EAGER)
    @JsonManagedReference("vehicule-images")
    @ToString.Exclude
    //@JsonManagedReference
    @JsonIgnore
    
    private List<Images> images = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    /**
     * modification apporter le 21-10-2025
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id")
    @JsonBackReference
    private Utilisateurs proprietaire;


}