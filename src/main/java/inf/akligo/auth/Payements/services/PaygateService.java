package inf.akligo.auth.Payements.services;

import inf.akligo.auth.Payements.dtos.CheckResponseDTO;
import inf.akligo.auth.Payements.dtos.CheckTransactionDto;
import inf.akligo.auth.Payements.dtos.ClientRequestDto;
import inf.akligo.auth.Payements.dtos.DepositResponseDto;

public interface PaygateService {

    Object depotransaction(Object data);
    DepositResponseDto depotTransactionPaygates(ClientRequestDto data);

    CheckResponseDTO checkTransactionStatus(CheckTransactionDto data);
}
