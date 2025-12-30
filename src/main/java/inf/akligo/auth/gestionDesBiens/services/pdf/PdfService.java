package inf.akligo.auth.gestionDesBiens.services.pdf;

import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
@Service
public class PdfService {
    
   public byte[] genererFacture(Reservation reservation) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("FACTURE DE RÉSERVATION")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Client : " + reservation.getUtilisateur().getNom()));
            document.add(new Paragraph("Email : " + reservation.getUtilisateur().getEmail()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Appartement : " +
                    reservation.getAppartement().getNom()));
            document.add(new Paragraph("Date début : " + reservation.getDateDebut()));
            document.add(new Paragraph("Date fin : " + reservation.getDateFin()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Montant total : " +
                    reservation.getMontant() + " €")
                    .setBold());

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Statut : " + reservation.getStatut()));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    // ========== MÉTHODE SPÉCIFIQUE POUR VÉHICULES ==========
    
    public byte[] genererFactureVehicule(Reservation reservation) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("FACTURE DE LOCATION DE VÉHICULE")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("=== INFORMATIONS CLIENT ===")
                    .setBold());
            document.add(new Paragraph("Client : " + reservation.getUtilisateur().getNom() + " " + 
                    reservation.getUtilisateur().getPrenoms()));
            document.add(new Paragraph("Email : " + reservation.getUtilisateur().getEmail()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("=== INFORMATIONS VÉHICULE ===")
                    .setBold());
            document.add(new Paragraph("Marque : " +
                    reservation.getVehicule().getMarque()));
            document.add(new Paragraph("Modèle : " +
                    reservation.getVehicule().getModele()));
            document.add(new Paragraph("Immatriculation : " +
                    reservation.getVehicule().getImmatriculation()));
            // document.add(new Paragraph("Couleur : " +
            //         reservation.getVehicule().getCouleur()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("=== PÉRIODE DE LOCATION ===")
                    .setBold());
            document.add(new Paragraph("Date début : " + reservation.getDateDebut()));
            document.add(new Paragraph("Date fin : " + reservation.getDateFin()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("=== MONTANT ===")
                    .setBold());
            document.add(new Paragraph("Montant total : " +
                    reservation.getMontant() + " €")
                    .setBold()
                    .setFontSize(14));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Statut : " + reservation.getStatut()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Merci de votre confiance !"));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF véhicule", e);
        }
    }
}