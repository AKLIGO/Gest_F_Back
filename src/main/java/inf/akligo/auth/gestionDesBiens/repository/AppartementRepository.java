package inf.akligo.auth.gestionDesBiens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutAppartement;
import java.util.List;
@Repository
public interface AppartementRepository extends JpaRepository<Appartement, Long>{
    List<Appartement> findByProprietaire(Utilisateurs proprietaire);
    List<Appartement> findByProprietaireId(Long proprietaireId);
    Optional<Appartement> findByIdAndStatut(Long id, StatutAppartement statut);
    Optional<Appartement> findByNom(String nom);


}