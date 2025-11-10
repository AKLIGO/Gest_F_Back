package inf.akligo.auth.Payements.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CheckResponseDTO {

    private String tx_reference;
    private String payment_reference;
    private String datetime;
    private String identifier;
    private String payment_method;
    private String phone_number;
    private int status;
}
