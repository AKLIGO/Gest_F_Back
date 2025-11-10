package inf.akligo.auth.Payements.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DepositResponseDto {
    private String tx_reference;
    private int status;
}
