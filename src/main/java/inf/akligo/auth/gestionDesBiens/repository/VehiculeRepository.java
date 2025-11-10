package inf.akligo.auth.gestionDesBiens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeVehicule;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutVehicule;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import java.util.List;
@Repository
public interface VehiculeRepository extends JpaRepository<Vehicules, Long>{

    Optional<Vehicules> findByIdAndStatut(Long id, StatutVehicule statut);
    Optional<Vehicules> findByMarque(String marque);
    List<Vehicules> findByProprietaire(Utilisateurs proprietaire);
    List<Vehicules> findByProprietaireId(Long proprietaireId);
     // Ajoute cette ligne pour Spring Data JPA
    Optional<Vehicules> findByImmatriculation(String immatriculation);
    List<Vehicules> findByPublieTrue();

}