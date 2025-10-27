package inf.akligo.auth.gestionDesBiens.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import org.springframework.stereotype.Repository;
import inf.akligo.auth.gestionDesBiens.entity.Paiement;
import java.util.List;

@Repository
public interface PaimentRepository extends JpaRepository<Paiement, Long>{
    List<Paiement> findByReservation(Reservation reservation);
}