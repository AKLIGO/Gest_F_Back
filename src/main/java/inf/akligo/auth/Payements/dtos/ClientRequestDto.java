package inf.akligo.auth.Payements.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ClientRequestDto {

    private String phone;
    private int amount;
    private String network;
}
