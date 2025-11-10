package inf.akligo.auth.Payements.controllers;


import inf.akligo.auth.Payements.dtos.CheckResponseDTO;
import inf.akligo.auth.Payements.dtos.CheckTransactionDto;
import inf.akligo.auth.Payements.dtos.ClientRequestDto;
import inf.akligo.auth.Payements.dtos.DepositResponseDto;
import inf.akligo.auth.Payements.services.PaygateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payGate")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentAPI {

    PaygateService paygateService;
    @PostMapping(path = "/deposit")
    public DepositResponseDto payment(@RequestBody ClientRequestDto entity){
        return this.paygateService.depotTransactionPaygates(entity);
    }

    @PostMapping(path = "/check-status")
    public CheckResponseDTO checkStatus(@RequestBody CheckTransactionDto entity){
        return this.paygateService.checkTransactionStatus(entity);
    }

}
