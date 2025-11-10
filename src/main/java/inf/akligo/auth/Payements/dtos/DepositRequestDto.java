package inf.akligo.auth.Payements.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DepositRequestDto {
    private String auth_token;
    private String phone_number;
    private int amount;
    private String description;
    private String identifier;
    private String network;
}
