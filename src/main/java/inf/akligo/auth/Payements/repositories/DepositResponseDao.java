package inf.akligo.auth.Payements.repositories;

import inf.akligo.auth.Payements.dtos.DepositResponseDto;
import inf.akligo.auth.Payements.entities.DepositResponsePaygates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositResponseDao extends JpaRepository<DepositResponsePaygates,Long> {
}
