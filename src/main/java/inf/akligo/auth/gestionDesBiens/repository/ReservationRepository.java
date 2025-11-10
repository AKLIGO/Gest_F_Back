package inf.akligo.auth.gestionDesBiens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutDeReservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeDeRervation;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

    // Rechercher par statut
    List<Reservation> findByStatut(StatutDeReservation statut);

    // Rechercher par type
    List<Reservation> findByType(TypeDeRervation type);

    // Rechercher par appartement
    List<Reservation> findByAppartement(Appartement appartement);

    //Rechercher par vehicule

    List<Reservation> findByVehicule(Vehicules vehicule);

    // Rechercher par utilisateur
    List<Reservation> findByUtilisateur(Utilisateurs utilisateur);

    // Rechercher par dates exactes
    List<Reservation> findByDateDebut(LocalDate dateDebut);
    List<Reservation> findByDateFin(LocalDate dateFin);

    // Rechercher toutes les réservations entre deux dates
    List<Reservation> findByDateDebutBetween(LocalDate start, LocalDate end);

    // Rechercher par statut et type
    List<Reservation> findByStatutAndType(StatutDeReservation statut, TypeDeRervation type);
    Optional<List<Reservation>> findByVehiculeAndStatut(Vehicules vehicule, StatutDeReservation statut);


    // Rechercher par utilisateur et statut
    List<Reservation> findByUtilisateurAndStatut(Utilisateurs utilisateur, StatutDeReservation statut);

    // Rechercher par appartement et statut
    List<Reservation> findByAppartementAndStatut(Appartement appartement, StatutDeReservation statut);

    List<Reservation> findByAppartementId(Long appartementId);

    // Lister les réservations liées aux véhicules
    List<Reservation> findByVehiculeIsNotNull();

    // Lister les réservations liées aux appartements
    List<Reservation> findByAppartementIsNotNull();

    // Optionnel : filtrer par utilisateur
    List<Reservation> findByVehiculeIsNotNullAndUtilisateurId(Long utilisateurId);
    List<Reservation> findByAppartementIsNotNullAndUtilisateurId(Long utilisateurId);
    
    // Nouvelles méthodes pour le filtrage par propriétaire
    List<Reservation> findByAppartementProprietaireId(Long proprietaireId);
    List<Reservation> findByVehiculeProprietaireId(Long proprietaireId);
    List<Reservation> findByUtilisateurId(Long utilisateur);
    List<Reservation> findByAppartementProprietaireIdOrVehiculeProprietaireId(Long appartementProprietaireId, Long vehiculeProprietaireId);

    // 🔹 Liste des réservations d'appartements d'un utilisateur
    List<Reservation> findByUtilisateur_IdAndAppartementIsNotNull(Long utilisateurId);
    // Récupère toutes les réservations véhicules d’un utilisateur
    List<Reservation> findByUtilisateurAndVehiculeIsNotNull(Utilisateurs utilisateur);
    // 🔹 Liste des réservations de véhicules d'un utilisateur
    List<Reservation> findByUtilisateur_IdAndVehiculeIsNotNull(Long utilisateurId);

    // 🔹 Optionnel : toutes les réservations d'un utilisateur
    //List<Reservation> findByUtilisateur(Utilisateurs utilisateur);

    // 🔹 Optionnel : filtrer par statut ou type
    //List<Reservation> findByUtilisateurAndStatut(Utilisateurs utilisateur, StatutDeReservation statut);
}