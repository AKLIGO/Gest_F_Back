package inf.akligo.auth.authConfiguration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import inf.akligo.auth.authConfiguration.entity.BlackListedToken;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListedTokenRepository extends JpaRepository<BlackListedToken,Long>{
    Optional<BlackListedToken> findByToken(String token);
}