package inf.akligo.auth.gestionDesBiens.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.gestionDesBiens.entity.Images;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Images, Long>{
    // Méthode pour rechercher par libellé
    Images findByLibelle(String libelle);
    
    // Méthode pour vérifier si une image existe par libellé
    boolean existsByLibelle(String libelle);
    void deleteByLibelle(String libelle);



    // Récupérer les images qui ne sont liées ni à un appartement ni à un véhicule
    List<Images> findByAppartementIsNullAndVehiculeIsNull();

       // Méthode pour rechercher par immatriculation
    //Optional<Vehicules> findByImmatriculation(String immatriculation);

    // Images liées à un véhicule
    List<Images> findByVehiculeIsNotNull();

    // Images liées à un appartement
    List<Images> findByAppartementIsNotNull();

     // Images liées à un appartement dont le propriétaire a un ID donné
    // @Query("SELECT i FROM Images i WHERE i.appartement.proprietaire.id = :proprietaireId")
    // List<Images> findByProprietaireAppartement(@Param("proprietaireId") Long proprietaireId);

    // Images liées à un véhicule dont le propriétaire a un ID donné
    // @Query("SELECT i FROM Images i WHERE i.vehicule.proprietaire.id = :proprietaireId")
    // List<Images> findByProprietaireVehicule(@Param("proprietaireId") Long proprietaireId);

        // Images d’appartements appartenant à un propriétaire
    List<Images> findByAppartementProprietaire(Utilisateurs proprietaire);

    // Images de véhicules appartenant à un propriétaire
    List<Images> findByVehiculeProprietaire(Utilisateurs proprietaire);
}