package inf.akligo.auth.authConfiguration.request;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Builder
@AllArgsConstructor
// @NoArgsConstructor
@Getter
@Setter
public class AuthenticationResponse{
    private String token;
    private Utilisateurs user; 
}