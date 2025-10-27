package inf.akligo.auth.authConfiguration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import inf.akligo.auth.authConfiguration.entity.Token;
import org.springframework.stereotype.Repository;

@Repository

public interface TokenRepository extends JpaRepository<Token, Long>{

    Optional<Token> findByToken(String token);

}