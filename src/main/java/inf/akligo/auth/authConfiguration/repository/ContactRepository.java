package inf.akligo.auth.authConfiguration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import inf.akligo.auth.authConfiguration.entity.ContactMessage;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<ContactMessage, Long>{

}
