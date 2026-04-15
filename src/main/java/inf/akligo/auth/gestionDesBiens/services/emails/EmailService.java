package inf.akligo.auth.gestionDesBiens.services.emails;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutDeReservation;
import inf.akligo.auth.gestionDesBiens.services.pdf.PdfService;
import jakarta.mail.internet.MimeMessage;

@Service("reservationEmailService")
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfService pdfService;

    public EmailService(JavaMailSender mailSender, PdfService pdfService) {
        this.mailSender = mailSender;
        this.pdfService = pdfService;
    }

    @Async
    public void envoyerEmailAvecFacture(Utilisateurs utilisateur, Reservation reservation) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(utilisateur.getEmail());
            helper.setSubject("Confirmation de votre réservation");
            helper.setText(
                    "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Votre réservation a bien été enregistrée.\n" +
                    "Veuillez trouver votre facture en pièce jointe.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe",
                    false
            );

            // Génération du PDF
            byte[] pdfFacture = pdfService.genererFacture(reservation);

            helper.addAttachment(
                    "facture-reservation-" + reservation.getId() + ".pdf",
                    new ByteArrayResource(pdfFacture)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Async
public void envoyerEmailChangementStatut(Utilisateurs utilisateur,
                                         Reservation reservation,
                                         StatutDeReservation ancienStatut) {

    String sujet;
    String message;

    switch (reservation.getStatut()) {

        case CONFIRMEE -> {
            sujet = "✅ Réservation confirmée";
            message = "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Bonne nouvelle ! Votre réservation a été CONFIRMÉE.\n\n" +
                    "Appartement : " + reservation.getAppartement().getNom() + "\n" +
                    "Dates : " + reservation.getDateDebut() + " → " + reservation.getDateFin() + "\n" +
                    "Montant : " + reservation.getMontant() + " €\n\n" +
                    "Votre facture est jointe.\n\n" +
                    "Cordialement,\nL'équipe";

            // 📎 Facture PDF uniquement si CONFIRMÉE
            envoyerEmailAvecFacture(utilisateur, reservation);
            return;
        }

        case ANNULEE -> {
            sujet = "❌ Réservation annulée";
            message = "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Votre réservation a été annulée.\n\n" +
                    "Si vous avez déjà payé, le remboursement sera traité.\n\n" +
                    "Cordialement,\nL'équipe";
        }

        default -> {
            sujet = "🔔 Mise à jour de votre réservation";
            message = "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Le statut de votre réservation est passé de " +
                    ancienStatut + " à " + reservation.getStatut() + ".\n\n" +
                    "Cordialement,\nL'équipe. Veuillez vous reconnecter pour passer au payement";
        }
    }

    envoyerEmailSimple(utilisateur.getEmail(), sujet, message);
}

public void envoyerEmailSimple(String to, String sujet, String contenu) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(sujet);
    message.setText(contenu);
    mailSender.send(message);
}

@Async
public void envoyerEmailNotificationProprietaireAppartement(String proprietaireEmail,
                                                            String proprietaireNom,
                                                            Utilisateurs client,
                                                            Reservation reservation) {
    if (proprietaireEmail == null || proprietaireEmail.isBlank()) {
        return;
    }

    String nomProprietaire = proprietaireNom != null ? proprietaireNom : "Proprietaire";

    String sujet = "Nouvelle reservation sur votre appartement";
    String contenu = "Bonjour " + nomProprietaire + ",\n\n" +
            "Une nouvelle reservation a ete effectuee sur votre appartement.\n\n" +
            "Appartement : " + (reservation.getAppartement() != null ? reservation.getAppartement().getNom() : "N/A") + "\n" +
            "Client : " + (client != null ? client.getNom() + " " + client.getPrenoms() : "N/A") + "\n" +
            "Periode : du " + reservation.getDateDebut() + " au " + reservation.getDateFin() + "\n" +
            "Montant : " + reservation.getMontant() + " EUR\n" +
            "Statut : " + reservation.getStatut() + "\n\n" +
            "Cordialement,\nL'equipe";

    envoyerEmailSimple(proprietaireEmail, sujet, contenu);
}

@Async
public void envoyerEmailNotificationProprietaireVehicule(String proprietaireEmail,
                                                         String proprietaireNom,
                                                         Utilisateurs client,
                                                         Reservation reservation) {
    if (proprietaireEmail == null || proprietaireEmail.isBlank()) {
        return;
    }

    String nomProprietaire = proprietaireNom != null ? proprietaireNom : "Proprietaire";

    String sujet = "Nouvelle reservation sur votre vehicule";
    String contenu = "Bonjour " + nomProprietaire + ",\n\n" +
            "Une nouvelle reservation a ete effectuee sur votre vehicule.\n\n" +
            "Vehicule : " + (reservation.getVehicule() != null
            ? reservation.getVehicule().getMarque() + " " + reservation.getVehicule().getModele()
            : "N/A") + "\n" +
            "Client : " + (client != null ? client.getNom() + " " + client.getPrenoms() : "N/A") + "\n" +
            "Periode : du " + reservation.getDateDebut() + " au " + reservation.getDateFin() + "\n" +
            "Montant : " + reservation.getMontant() + " EUR\n" +
            "Statut : " + reservation.getStatut() + "\n\n" +
            "Cordialement,\nL'equipe";

    envoyerEmailSimple(proprietaireEmail, sujet, contenu);
}

// ========== MÉTHODES SPÉCIFIQUES POUR VÉHICULES ==========

@Async
public void envoyerEmailAvecFactureVehicule(Utilisateurs utilisateur, Reservation reservation) {

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(utilisateur.getEmail());
        helper.setSubject("🚗 Confirmation de votre réservation de véhicule");
        helper.setText(
                "Bonjour " + utilisateur.getNom() + ",\n\n" +
                "Votre réservation de véhicule a bien été enregistrée.\n\n" +
                "Véhicule : " + reservation.getVehicule().getMarque() + " " + 
                reservation.getVehicule().getModele() + "\n" +
                "Immatriculation : " + reservation.getVehicule().getImmatriculation() + "\n" +
                "Période : du " + reservation.getDateDebut() + " au " + reservation.getDateFin() + "\n\n" +
                "Veuillez trouver votre facture en pièce jointe.\n\n" +
                "Cordialement,\n" +
                "L'équipe",
                false
        );

        // Génération du PDF pour véhicule
        byte[] pdfFacture = pdfService.genererFactureVehicule(reservation);

        helper.addAttachment(
                "facture-location-vehicule-" + reservation.getId() + ".pdf",
                new ByteArrayResource(pdfFacture)
        );

        mailSender.send(message);

    } catch (Exception e) {
        throw new RuntimeException("Erreur lors de l'envoi de l'email véhicule", e);
    }
}

@Async
public void envoyerEmailChangementStatutVehicule(Utilisateurs utilisateur,
                                                  Reservation reservation,
                                                  StatutDeReservation ancienStatut) {

    String sujet;
    String message;

    switch (reservation.getStatut()) {

        case CONFIRMEE -> {
            sujet = "✅ Réservation de véhicule confirmée";
            message = "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Bonne nouvelle ! Votre réservation de véhicule a été CONFIRMÉE.\n\n" +
                    "Véhicule : " + reservation.getVehicule().getMarque() + " " + 
                    reservation.getVehicule().getModele() + "\n" +
                    "Immatriculation : " + reservation.getVehicule().getImmatriculation() + "\n" +
                    "Dates : " + reservation.getDateDebut() + " → " + reservation.getDateFin() + "\n" +
                    "Montant : " + reservation.getMontant() + " €\n\n" +
                    "Votre facture est jointe.\n\n" +
                    "Cordialement,\nL'équipe";

            // 📎 Facture PDF uniquement si CONFIRMÉE
            envoyerEmailAvecFactureVehicule(utilisateur, reservation);
            return;
        }

        case ANNULEE -> {
            sujet = "❌ Réservation de véhicule annulée";
            message = "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Votre réservation de véhicule a été annulée.\n\n" +
                    "Véhicule : " + reservation.getVehicule().getMarque() + " " + 
                    reservation.getVehicule().getModele() + "\n" +
                    "Immatriculation : " + reservation.getVehicule().getImmatriculation() + "\n\n" +
                    "Si vous avez déjà payé, le remboursement sera traité.\n\n" +
                    "Cordialement,\nL'équipe";
        }

        default -> {
            sujet = "🔔 Mise à jour de votre réservation de véhicule";
            message = "Bonjour " + utilisateur.getNom() + ",\n\n" +
                    "Le statut de votre réservation de véhicule est passé de " +
                    ancienStatut + " à " + reservation.getStatut() + ".\n\n" +
                    "Véhicule : " + reservation.getVehicule().getMarque() + " " + 
                    reservation.getVehicule().getModele() + "\n" +
                    "Immatriculation : " + reservation.getVehicule().getImmatriculation() + "\n\n" +
                    "Cordialement,\nL'équipe. Veuillez vous reconnecter pour passer au payement";
        }
    }

    envoyerEmailSimple(utilisateur.getEmail(), sujet, message);
}


}   
