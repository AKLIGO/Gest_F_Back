package inf.akligo.auth.authConfiguration.servicesCompte;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.repository.ResetPasswordRepository;
import inf.akligo.auth.authConfiguration.entity.PasswordResetCode;
@Service
public class PasswordResetService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final MailService mailService;
    private final ResetPasswordRepository resetPasswordRepository;
    public PasswordResetService(MailService mailService, ResetPasswordRepository resetPasswordRepository) {
        this.mailService = mailService;
        this.resetPasswordRepository = resetPasswordRepository;
    }

    public String generateCode(){
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    public PasswordResetCode createResetCode(Utilisateurs utilisateur){
        String code = generateCode();
        return PasswordResetCode.builder()
                .code(code)
                .utilisateur(utilisateur)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(15))
                .build();
    }

    public void sendResetCode(Utilisateurs utilisateur, String code){
        // Logique pour envoyer le code par email ou SMS
        String subject = "Code de renitialisation du mot de passe";
        String body = "Hello, \n\n " +
                "voici votre code de reinitialisation : " + code + "\n\n" +
                "ce code est valide pendant 10 minutes.\n\n"+
                "si vous n'etes pas a l'origine de cette demande, ignorez ce message.";
        // Utiliser un service d'email pour envoyer le message
        System.out.println("Envoi de l'email à " + utilisateur.getEmail());
        System.out.println("Sujet: " + subject);
        System.out.println("Corps: " + body);

        mailService.sendEmail(utilisateur.getEmail(), subject, body);
    }
    
}
