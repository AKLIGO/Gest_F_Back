package inf.akligo.auth.authConfiguration.datas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProprietaireContactDTO {
    private String nom;
    private String email;
    private String telephone;
    private String mailtoLink;  // Lien mailto: pour ouvrir directement le client email
}
