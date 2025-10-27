package inf.akligo.auth.gestionDesBiens.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import inf.akligo.auth.gestionDesBiens.entity.Immeuble;

@Repository
public interface ImmeubleRepository extends JpaRepository<Immeuble, Long>{

}