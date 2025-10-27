package inf.akligo.auth.gestionDesBiens.services.serviceFacture;

import inf.akligo.auth.gestionDesBiens.entity.Facture;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeFacture;
import inf.akligo.auth.gestionDesBiens.repository.FactureRepository;
import inf.akligo.auth.gestionDesBiens.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.*;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final ReservationRepository reservationRepository;

    private static final String FACTURE_DIR ="uploads/factures/";

    /**
     * Generer une facture provisoire ou final au format PDF et enregistrer son nom en base.
     */

    public Facture genererFacture(Long reservartionId, TypeFacture typeFacture, boolean solde) throws Exception {
        /**
         * verifier la reservation
         */

        Reservation reservation = reservationRepository.findById(reservartionId)
                    .orElseThrow(() -> new RuntimeException("reservation non trouver"));
        /**
         * creer le dossier de facture s'il n'existe pas
         */

        File dir =new File(FACTURE_DIR);
        if(!dir.exists()) dir.mkdirs();

        /**
         * generer le nom du fichier
         */

        String fileName ="facture_" + reservartionId + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = FACTURE_DIR + fileName;

        /**
         * Preparer les parametres du rapport Jasper
         */

        JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/reports/facture.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("nomClient", reservation.getUtilisateur().getNom());
        params.put("NumeroClient", reservation.getUtilisateur().getTelephone());
        params.put("montant", reservation.getMontant());
        params.put("typeFacture", typeFacture.toString());
        params.put("dateEmission", LocalDate.now().toString());
        params.put("numeroFacture", genererNumeroFacture(reservartionId));

        /**
         * Ajout des infos lier a l'appartement
         */

        if(reservation.getAppartement() !=null){
            params.put("nomAppartement", reservation.getAppartement().getNom());
            params.put("adresseAppartement", reservation.getAppartement().getAdresse());
            params.put("logoPath", "src/main/resources/reports/logo.png");
        } else {
            params.put("nomAppartement", "N/A");
            params.put("adresseAppartement","N/A");
        }

        /**
         * Generation du fichier PDF
         */

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
        JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);

        /**
         * Enregistrer les infos en base
         */

        Facture facture = Facture.builder()
                .dateEmission(LocalDate.now())
                .montant(reservation.getMontant())
                .montantTotal(reservation.getMontant())
                .typeFacture(typeFacture)
                .numeroFacture(params.get("numeroFacture").toString())
                .cheminFichier(fileName)
                .reservation(reservation)
                .solde(solde)
                .build();


        return factureRepository.save(facture);
    }

    
    /**
     * Récupèrer le contenu PDF d’une facture à partir de son ID de réservation.
     */

    public byte[] getFacturePdf(Long reservartionId) throws IOException {
        Facture facture = factureRepository.findByReservationId(reservartionId)
                .orElseThrow(() -> new RuntimeException("Facture non trouver"));
        
        Path filePath =Paths.get(FACTURE_DIR + facture.getCheminFichier());
        return Files.readAllBytes(filePath);
    }

    /**
     * generer un numero unique de facture 
     */
    
    private String genererNumeroFacture(Long reservationId) {
        String datePart = LocalDate.now().toString().replace("-", "");
        String idPart = String.format("%04d", reservationId);
        return "FAC-" + datePart + "-" + idPart;
    }
}