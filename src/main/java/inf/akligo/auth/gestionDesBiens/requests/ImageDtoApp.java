package inf.akligo.auth.gestionDesBiens.requests;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageDtoApp{
    private Long id;
    private String libelle;
    private String nomFichier;
    private String typeMime;
    private Long appartementId;
    
}