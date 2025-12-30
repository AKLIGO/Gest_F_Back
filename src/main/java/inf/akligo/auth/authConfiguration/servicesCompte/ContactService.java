package inf.akligo.auth.authConfiguration.servicesCompte;
import inf.akligo.auth.authConfiguration.entity.ContactMessage;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;
import inf.akligo.auth.authConfiguration.repository.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactService{
    private final ContactRepository contactRepository;
    private final JavaMailSender mailSender;
    
    @Value("${contact.email.recipient}")
    private String recipientEmail;

    public ContactMessage saveMessage(ContactMessage message){
        // Sauvegarder le message dans la base de données
        ContactMessage savedMessage = contactRepository.save(message);
        
        // Envoyer un email à l'adresse configurée
        envoyerEmailContact(savedMessage);
        
        return savedMessage;
    }
    
    private void envoyerEmailContact(ContactMessage contact) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(recipientEmail);
            mailMessage.setSubject("📧 Nouveau message de contact : " + contact.getSubject());
            mailMessage.setReplyTo(contact.getEmail());
            mailMessage.setText(
                "Vous avez reçu un nouveau message de contact.\n\n" +
                "De : " + contact.getName() + "\n" +
                "Email : " + contact.getEmail() + "\n" +
                "Sujet : " + contact.getSubject() + "\n\n" +
                "Message :\n" +
                contact.getMessage() + "\n\n" +
                "---\n" +
                "Vous pouvez répondre directement à cet email pour contacter l'expéditeur."
            );
            
            mailSender.send(mailMessage);
        } catch (Exception e) {
            // Log l'erreur mais ne bloque pas la sauvegarde
            System.err.println("Erreur lors de l'envoi de l'email de contact : " + e.getMessage());
        }
    }
}