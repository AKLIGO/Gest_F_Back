package inf.akligo.auth.gestionDesBiens.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO contenant les informations sur la possibilité d'annulation d'une réservation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancellationInfoDTO {
    
    /**
     * Indique si la réservation peut être annulée
     */
    private boolean canCancel;
    
    /**
     * Message expliquant pourquoi l'annulation est possible ou non
     */
    private String message;
    
    /**
     * Nombre d'heures restantes avant la fin de la période d'annulation (si applicable)
     */
    private Long hoursRemaining;
    
    /**
     * ID de la réservation
     */
    private Long reservationId;
}
