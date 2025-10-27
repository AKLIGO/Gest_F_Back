package inf.akligo.auth.gestionDesBiens.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeImmeuble;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.EntityListeners;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import java.util.List;
import lombok.ToString;
import java.time.LocalDateTime;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "utilisateur")
@JsonIgnoreProperties({"utilisateur"})
@EntityListeners(AuditingEntityListener.class)
public class Immeuble{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nbrAppartment;
    private int nbrEtage;
    private String nom;
    private String ville;
    private String description;
    @Enumerated(EnumType.STRING)
    private TypeImmeuble type;

    @Builder.Default
    @OneToMany(mappedBy="immeuble",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("immeuble-appartement")
    private List<Appartement> appartements=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateurs utilisateur;

    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

}