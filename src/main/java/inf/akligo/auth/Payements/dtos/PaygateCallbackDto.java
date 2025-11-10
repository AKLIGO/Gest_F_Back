package inf.akligo.auth.Payements.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaygateCallbackDto {

    private String tx_reference;
    private String identifier;
    private String payment_reference;
    private Double amount;
    private String datetime;
    private String payment_method;
    private String phone_number;
}
