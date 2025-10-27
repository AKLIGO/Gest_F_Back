package inf.akligo.auth.authConfiguration.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.ResponseEntity;
import inf.akligo.auth.securityConfig.security.BlackListTokenSevice;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.servlet.http.HttpServletRequest;
import inf.akligo.auth.authConfiguration.datas.UserDto;
//import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.entity.Roles;
import inf.akligo.auth.authConfiguration.entity.Token;
import inf.akligo.auth.authConfiguration.entity.BlackListedToken;
import inf.akligo.auth.authConfiguration.repository.TokenRepository;
import inf.akligo.auth.authConfiguration.request.AuthenticationRequest;
import inf.akligo.auth.authConfiguration.request.AuthenticationResponse;

import inf.akligo.auth.authConfiguration.servicesCompte.ServiceCompteImpl;
import inf.akligo.auth.authConfiguration.datas.RoleUser;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestParam;
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/auth")
@RestController

public class UserController{

    private ServiceCompteImpl serviceCompteImpl;
    private BlackListTokenSevice blacklistTokenService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(ServiceCompteImpl serviceCompteImpl,
                        BlackListTokenSevice blacklistTokenService){
        this.serviceCompteImpl=serviceCompteImpl;
        this.blacklistTokenService = blacklistTokenService;

    }
    @GetMapping(path="/listUser")
    public List<Utilisateurs> listeUtilisateur(){

        return serviceCompteImpl.listeUtilisateur();

    }

    @PostMapping(path="/addUser")
    public Utilisateurs ajouterUtilisateur(@RequestBody Utilisateurs utilisateur){
        return serviceCompteImpl.ajouterUtilisateur(utilisateur);
    }

    @PostMapping(path="/addRole")

    public Roles ajouterRole(@RequestBody Roles role){
        return serviceCompteImpl.ajouterRole(role);
    }

    @PostMapping(path="/addRoleAUtilisateur")
    public void ajouterRoleAUtilisateur(@RequestBody RoleUser roleUser){
        serviceCompteImpl.ajouterRoleAUtilisateur(roleUser.getEmail(),roleUser.getName());
    }

    @DeleteMapping(path="/supprimerUtilisateur/{id}")

    public void supprimerUtilisateur(@PathVariable Long id){
        serviceCompteImpl.supprimerUtilisateurParId(id);
    }

    @PutMapping(path="/modifierUtilisateur/{id}")
    public Utilisateurs modifierUtilisateur( @RequestBody Utilisateurs utilisateurDetails,
    @PathVariable Long id){
        return serviceCompteImpl.modifierUtilisateur(utilisateurDetails,id);
    }

    @PostMapping(path="/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
        @RequestBody @Valid AuthenticationRequest request
    ){
        

        return ResponseEntity.ok(serviceCompteImpl.authenticate(request));
    }


    @PostMapping("/activation-account")
    public ResponseEntity<?> confirm(
        @RequestParam String token
    ) {
        try {
            serviceCompteImpl.activationAccount(token);
            return ResponseEntity.ok("Compte activé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

   // Endpoint de déconnexion
    @PostMapping("/logout")
  
public ResponseEntity<?> logout(HttpServletRequest request) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            logger.info("User {} is attempting to logout", userEmail);

            // Ajouter le token à la blacklist
            String token = extractTokenFromRequest(request);
            if (token != null) {
                logger.info("Extracted token for logout: {}", token);
                blacklistTokenService.blackListToken(token, userEmail, "LOGOUT");
            } else {
                logger.warn("No token found in the request");
                return ResponseEntity.badRequest().body("Token manquant");
            }

            // Nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();
            logger.info("User {} successfully logged out", userEmail);
            return ResponseEntity.ok("Déconnexion réussie");
        } else {
            logger.warn("No active session found for logout attempt");
            return ResponseEntity.badRequest().body("Aucune session active");
        }
    } catch (Exception e) {
        logger.error("Error during logout: {}", e.getMessage());
        return ResponseEntity.status(500).body("Erreur lors de la déconnexion: " + e.getMessage());
    }
}

     // Méthode utilitaire pour extraire le token de la requête
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }


@GetMapping("/users/me")
public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
    System.out.println("Appel de /users/me");
    
    Optional<UserDto> currentUser = serviceCompteImpl.getCurrentUser(authentication);
    if (currentUser.isPresent()) {
        System.out.println("Utilisateur trouvé: " + currentUser.get().getEmail());
        return ResponseEntity.ok(currentUser.get());
    } else {
        System.out.println("Utilisateur non authentifié");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}




}