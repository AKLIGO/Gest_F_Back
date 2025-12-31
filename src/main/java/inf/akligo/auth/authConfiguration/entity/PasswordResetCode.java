package inf.akligo.auth.authConfiguration.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.time.LocalDateTime;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;  

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetCode {

@Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String code;

@OneToOne(optional = false)
@JoinColumn(name = "utilisateur_id") 
private Utilisateurs utilisateur;


private LocalDateTime expiresAt;
    
}
