package inf.akligo.auth.gestionDesBiens.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.time.LocalDate;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutPaiement;
import inf.akligo.auth.gestionDesBiens.enumerateurs.ModePaiement;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.EntityListeners;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "reservation")
public class Paiement{

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate datePaiement;

    private double montant;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement; // ESPECES, MOBILE_MONEY, VIREMENT, CARTE

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut; // EFFECTUE, EN_ATTENTE, ANNULE

    @ManyToOne
    @JsonBackReference
    @ToString.Exclude 
    private Reservation reservation;
}