package inf.akligo.auth.authConfiguration.entity;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;



@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data

public class Token{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validateAt;


    @ManyToOne
    @JoinColumn(name="utilisateurId", nullable=false)
    private Utilisateurs utilisateur;

     // Méthode pour obtenir la date d'expiration
    public LocalDateTime getExpiration() {
        return expiresAt; // Renvoie expiresAt
    }

    // Méthode pour obtenir l'utilisateur
    public Utilisateurs getUser() {
        return utilisateur; // Renvoie l'utilisateur associé
    }



}