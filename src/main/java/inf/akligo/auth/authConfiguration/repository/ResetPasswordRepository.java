package inf.akligo.auth.authConfiguration.repository;
import inf.akligo.auth.authConfiguration.entity.PasswordResetCode;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByCode(String code);
      Optional<PasswordResetCode> findByCodeAndUtilisateur(String code, Utilisateurs utilisateur);

    // ✅ Supprime tous les codes liés à un utilisateur
    void deleteByUtilisateur(Utilisateurs utilisateur); 
    
}
