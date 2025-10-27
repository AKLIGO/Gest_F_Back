package inf.akligo.auth.authConfiguration.request;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
// @NoArgsConstructor
@Getter
@Setter
@Builder
public class AuthenticationRequest{

    @NotEmpty(message = "password is mandatory")
    @NotBlank(message="password is mandatory")
    @Size(min=8, message="Password should be 8 characters long minimuim")
    private String password;
    
    @NotEmpty(message = "Email is not fromatted")
    @NotBlank(message="Email is not fromatted")
    private String email;
    
}