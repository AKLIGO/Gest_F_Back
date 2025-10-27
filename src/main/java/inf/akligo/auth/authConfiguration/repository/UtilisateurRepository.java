package inf.akligo.auth.authConfiguration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;


@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateurs,Long>{

    Optional<Utilisateurs> findByEmail(String email);
}