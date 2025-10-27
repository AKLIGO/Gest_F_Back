package inf.akligo.auth.gestionDesBiens.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutDeReservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeDeRervation;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.gestionDesBiens.entity.Paiement;
//import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.ToString;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
//@ToString(exclude = "paiements")
public class Reservation{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montant;
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;


    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;


    @ManyToOne
    @JoinColumn(name = "appartement_id")
    private Appartement appartement;

    @ManyToOne
    @JoinColumn(name="vehicule_id")
    private Vehicules vehicule;


    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateurs utilisateur;

    @Enumerated(EnumType.STRING)
    private StatutDeReservation statut;

    @Enumerated(EnumType.STRING)
    private TypeDeRervation type;

    //@JsonIgnore
    @JsonManagedReference
    @ToString.Exclude 
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Paiement> paiements = new ArrayList<>();


    // Retourne 0 si paiements est null
    public double getMontantPaye() {
        return paiements == null ? 0 : paiements.stream().mapToDouble(Paiement::getMontant).sum();
    }

    public boolean isTotalementPaye() {
        return getMontantPaye() >= montant;
    }

  


}