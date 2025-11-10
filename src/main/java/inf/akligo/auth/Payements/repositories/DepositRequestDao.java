package inf.akligo.auth.Payements.repositories;

import inf.akligo.auth.Payements.entities.DepositRequestPaygates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DepositRequestDao extends JpaRepository<DepositRequestPaygates,Long> {
}
