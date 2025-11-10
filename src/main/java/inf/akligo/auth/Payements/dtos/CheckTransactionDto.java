package inf.akligo.auth.Payements.dtos;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CheckTransactionDto {
    private String tx_reference;
    private String auth_token;
}
