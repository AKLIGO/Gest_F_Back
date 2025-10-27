package inf.akligo.auth.gestionDesBiens.requests;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Builder
// @Getter
// @Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTOVeh{
    private Long id;
    private String libelle;
    private String nomFichier;
    private String typeMime;
    private Long vehiculeId;
    private String previewUrl;
}