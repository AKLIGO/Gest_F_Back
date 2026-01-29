package inf.akligo.auth.gestionDesBiens.services.excel;

import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseDTO;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;
import inf.akligo.auth.gestionDesBiens.requests.PaiementDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exporte la liste des réservations d'appartements en Excel
     */
    public byte[] exportReservationsAppartements(List<ReservationResponseDTO> reservations) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Réservations Appartements");

            // Style pour l'en-tête
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Style pour les données
            CellStyle dataStyle = createDataStyle(workbook);

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nom Appartement", "Adresse", "Date Début", "Date Fin", 
                               "Montant (€)", "Client Nom", "Client Prénoms", "Statut"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            int rowNum = 1;
            for (ReservationResponseDTO reservation : reservations) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, reservation.getId(), dataStyle);
                createCell(row, 1, reservation.getAppartementNom(), dataStyle);
                createCell(row, 2, reservation.getAppartementAdresse(), dataStyle);
                createCell(row, 3, reservation.getDateDebut() != null ? 
                          reservation.getDateDebut().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 4, reservation.getDateFin() != null ? 
                          reservation.getDateFin().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 5, reservation.getMontant(), dataStyle);
                createCell(row, 6, reservation.getUtilisateurNom(), dataStyle);
                createCell(row, 7, reservation.getUtilisateurPrenoms(), dataStyle);
                createCell(row, 8, reservation.getStatut(), dataStyle);
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Exporte la liste des réservations de véhicules en Excel
     */
    public byte[] exportReservationsVehicules(List<ReservationResponseVehi> reservations) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Réservations Véhicules");

            // Style pour l'en-tête
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Style pour les données
            CellStyle dataStyle = createDataStyle(workbook);

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Marque Véhicule", "Immatriculation", "Date Début", "Date Fin", 
                               "Montant (€)", "Client Nom", "Client Prénoms", "Statut"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            int rowNum = 1;
            for (ReservationResponseVehi reservation : reservations) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, reservation.getId(), dataStyle);
                createCell(row, 1, reservation.getVehiculeMarque(), dataStyle);
                createCell(row, 2, reservation.getVehiculeImmatriculation(), dataStyle);
                createCell(row, 3, reservation.getDateDebut() != null ? 
                          reservation.getDateDebut().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 4, reservation.getDateFin() != null ? 
                          reservation.getDateFin().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 5, reservation.getMontant(), dataStyle);
                createCell(row, 6, reservation.getUtilisateurNom(), dataStyle);
                createCell(row, 7, reservation.getUtilisateurPrenoms(), dataStyle);
                createCell(row, 8, reservation.getStatut(), dataStyle);
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Crée le style pour l'en-tête
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crée le style pour les données
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crée une cellule avec une valeur String
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * Crée une cellule avec une valeur Long
     */
    private void createCell(Row row, int column, Long value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    /**
     * Crée une cellule avec une valeur Double
     */
    private void createCell(Row row, int column, Double value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    /**
     * Exporte la liste de tous les paiements en Excel
     */
    public byte[] exportAllPaiements(List<PaiementDTO> paiements) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tous les Paiements");

            // Style pour l'en-tête
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Style pour les données
            CellStyle dataStyle = createDataStyle(workbook);

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Paiement", "Date Paiement", "Montant (€)", "Mode Paiement", 
                               "Statut", "ID Réservation", "Client Nom", "Client Téléphone"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            int rowNum = 1;
            for (PaiementDTO paiement : paiements) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, paiement.getId(), dataStyle);
                createCell(row, 1, paiement.getDatePaiement() != null ? 
                          paiement.getDatePaiement().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 2, paiement.getMontant(), dataStyle);
                createCell(row, 3, paiement.getModePaiement() != null ? 
                          paiement.getModePaiement().name() : "", dataStyle);
                createCell(row, 4, paiement.getStatut() != null ? 
                          paiement.getStatut().name() : "", dataStyle);
                createCell(row, 5, paiement.getReservationId(), dataStyle);
                createCell(row, 6, paiement.getUtilisateurNom(), dataStyle);
                createCell(row, 7, paiement.getUtilisateurTelephone(), dataStyle);
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Exporte les paiements liés à une réservation spécifique en Excel
     */
    public byte[] exportPaiementsByReservation(List<PaiementDTO> paiements, Long reservationId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Paiements Réservation " + reservationId);

            // Style pour l'en-tête
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Style pour les données
            CellStyle dataStyle = createDataStyle(workbook);

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Paiement", "Date Paiement", "Montant (€)", "Mode Paiement", 
                               "Statut", "Client Nom", "Client Téléphone"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            int rowNum = 1;
            double totalMontant = 0.0;
            
            for (PaiementDTO paiement : paiements) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, paiement.getId(), dataStyle);
                createCell(row, 1, paiement.getDatePaiement() != null ? 
                          paiement.getDatePaiement().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, 2, paiement.getMontant(), dataStyle);
                createCell(row, 3, paiement.getModePaiement() != null ? 
                          paiement.getModePaiement().name() : "", dataStyle);
                createCell(row, 4, paiement.getStatut() != null ? 
                          paiement.getStatut().name() : "", dataStyle);
                createCell(row, 5, paiement.getUtilisateurNom(), dataStyle);
                createCell(row, 6, paiement.getUtilisateurTelephone(), dataStyle);
                
                if (paiement.getStatut() != null && paiement.getStatut().name().equals("EFFECTUE")) {
                    totalMontant += paiement.getMontant();
                }
            }

            // Ajouter une ligne de total
            Row totalRow = sheet.createRow(rowNum + 1);
            Cell totalLabelCell = totalRow.createCell(1);
            totalLabelCell.setCellValue("TOTAL PAYÉ:");
            
            CellStyle totalStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            totalStyle.setFont(boldFont);
            totalLabelCell.setCellStyle(totalStyle);
            
            Cell totalValueCell = totalRow.createCell(2);
            totalValueCell.setCellValue(totalMontant);
            totalValueCell.setCellStyle(totalStyle);

            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
