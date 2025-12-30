package inf.akligo.auth.gestionDesBiens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutAppartement;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AppartementRepository extends JpaRepository<Appartement, Long>{
    List<Appartement> findByProprietaire(Utilisateurs proprietaire);
    List<Appartement> findByProprietaireId(Long proprietaireId);
    Optional<Appartement> findByIdAndStatut(Long id, StatutAppartement statut);
    Optional<Appartement> findByNom(String nom);
    List<Appartement> findByPublieTrue();

    //16-12-2025
     List<Appartement> findByPublieTrueAndPrixBetween(Double prixMin, Double prixMax);

    List<Appartement> findByPublieTrueAndAdresseContainingIgnoreCaseAndPrixBetween(
        String adresse,
        double prixMin,
        double prixMax
);

@Query("""
    SELECT a FROM Appartement a
    WHERE a.publie = true
    AND (:adresse IS NULL OR LOWER(a.adresse) LIKE LOWER(CONCAT('%', :adresse, '%')))
    AND (:prixMin IS NULL OR a.prix >= :prixMin)
    AND (:prixMax IS NULL OR a.prix <= :prixMax)
""")
List<Appartement> rechercherAppartementsDisponibles(
        @Param("adresse") String adresse,
        @Param("prixMin") Double prixMin,
        @Param("prixMax") Double prixMax
);

    


}