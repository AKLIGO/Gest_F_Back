package inf.akligo.auth.authConfiguration.servicesCompte;

public interface MailService {
    void sendEmail(String to, String subject, String body);
}
