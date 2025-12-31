package inf.akligo.auth.authConfiguration.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import inf.akligo.auth.authConfiguration.repository.ResetPasswordRepository;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import inf.akligo.auth.authConfiguration.servicesCompte.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import inf.akligo.auth.authConfiguration.entity.PasswordResetCode;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RequestMapping("/api/password-reset")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    private final ResetPasswordRepository resetPasswordRepository;  
    private final UtilisateurRepository utilisateurRepository;

    // Demande de code de reinitialisation
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email){
        utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
            // supprimer les anciens code 
            resetPasswordRepository.deleteByUtilisateur(utilisateur);
            // générer et envoyer un nouveau code
            PasswordResetCode resetCode = passwordResetService.createResetCode(utilisateur);
            resetPasswordRepository.save(resetCode);

            // envoyer le code par email 
            passwordResetService.sendResetCode(utilisateur, resetCode.getCode());
        });
        return ResponseEntity.ok().build();
    }

    // Verification du code de reinitialisation
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code){
            Optional<Utilisateurs> userOpt = utilisateurRepository.findByEmail(email);
            if (userOpt.isEmpty()){
                return ResponseEntity.badRequest().body("Utilisateur non trouvé");
            }
            Utilisateurs utilisateur = userOpt.get();
            Optional<PasswordResetCode> resetCodeOpt = resetPasswordRepository.findByCodeAndUtilisateur(code, utilisateur);
            if (resetCodeOpt.isEmpty() || resetCodeOpt.get().getExpiresAt().isBefore(java.time.LocalDateTime.now())){
                return ResponseEntity.badRequest().body("Code invalide ou expiré");
            }
            return ResponseEntity.ok().build();

    }  
    
    // changement du mot de passe
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String code, @RequestParam String newPassword){
            Optional<Utilisateurs> userOpt = utilisateurRepository.findByEmail(email);
            if (userOpt.isEmpty()){
                return ResponseEntity.badRequest().body("Utilisateur non trouvé");
            }
            Utilisateurs utilisateur = userOpt.get();
            Optional<PasswordResetCode> resetCodeOpt = resetPasswordRepository.findByCodeAndUtilisateur(code, utilisateur);
            if (resetCodeOpt.isEmpty() || resetCodeOpt.get().getExpiresAt().isBefore(java.time.LocalDateTime.now())){
                return ResponseEntity.badRequest().body("Code invalide ou expiré");
            }
            // Encode le mot de passe avant sauvegarde
        utilisateur.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPassword));
        utilisateurRepository.save(utilisateur);
        // Supprimer le code de reinitialisation après utilisation
        resetPasswordRepository.delete(resetCodeOpt.get());
        return ResponseEntity.ok().build();
            // mettre a jour le mot de passe
            
}
}