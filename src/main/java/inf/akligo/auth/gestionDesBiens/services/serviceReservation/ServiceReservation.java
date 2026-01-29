
package inf.akligo.auth.gestionDesBiens.services.serviceReservation;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import inf.akligo.auth.gestionDesBiens.entity.Vehicules;
import inf.akligo.auth.authConfiguration.entity.Roles;
import java.util.Collections;
import java.util.stream.Collectors;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;
import inf.akligo.auth.gestionDesBiens.repository.ReservationRepository;
import inf.akligo.auth.gestionDesBiens.repository.AppartementRepository;
import inf.akligo.auth.gestionDesBiens.repository.VehiculeRepository;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.entity.Reservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.StatutDeReservation;
import inf.akligo.auth.gestionDesBiens.enumerateurs.TypeDeRervation;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import java.io.IOException;
import inf.akligo.auth.gestionDesBiens.requests.ReservationRequest;
import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseDTO;
import inf.akligo.auth.authConfiguration.repository.RoleRepository;
import inf.akligo.auth.gestionDesBiens.requests.ReservationRequestVehi;
import inf.akligo.auth.gestionDesBiens.requests.CancellationInfoDTO;
// import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import inf.akligo.auth.gestionDesBiens.services.emails.EmailService;

@RequiredArgsConstructor
@Service
public class ServiceReservation{

    private final ReservationRepository reservationRepository;
    private final AppartementRepository appartementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;


    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        // Récupérer le username depuis le token via SecurityContextHolder

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Récupérer l'utilisateur connecté
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
       
        // s'assurer que l'utilisateur a le role connecter
        ensureClientRole(utilisateur);


        // Vérifier si l’appartement existe
        Appartement appartement = appartementRepository.findById(request.getAppartementId())
                .orElseThrow(() -> new RuntimeException("Appartement introuvable"));

        long jours = ChronoUnit.DAYS.between(request.getDateDebut(), request.getDateFin());
        if(jours<=0) throw new RuntimeException("Date invalide");

        double montant=jours * appartement.getPrix();

        // Vérifier si déjà réservé
        boolean dejaReserve = reservationRepository
                .findByAppartementAndStatut(appartement, StatutDeReservation.CONFIRMEE)
                .stream()
                .anyMatch(r ->
                        !(request.getDateFin().isBefore(r.getDateDebut()) ||
                          request.getDateDebut().isAfter(r.getDateFin()))
                );

        if (dejaReserve) {
            throw new RuntimeException("Appartement déjà réservé à ces dates !");
        }

        // Créer la réservation avec valeurs par défaut
        Reservation reservation = Reservation.builder()
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .type(TypeDeRervation.APPARTEMENT)
                .statut(StatutDeReservation.EN_ATTENTE) 
                .appartement(appartement)
                .montant(montant)
                .utilisateur(utilisateur)
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);
        // Email + facture PDF
        emailService.envoyerEmailAvecFacture(utilisateur, savedReservation);
        return savedReservation;
    
    
    }

    private void ensureClientRole(Utilisateurs utilisateur) {
            boolean hasClientRole = utilisateur.getRoles() != null && utilisateur.getRoles().stream()
                    .anyMatch(r -> "CLIENT".equalsIgnoreCase(r.getName()));
            if (!hasClientRole) {
                Roles clientRole = roleRepository.findByName("CLIENT")
                        .orElseThrow(() -> new RuntimeException("Role CLIENT non trouvé"));
                if (utilisateur.getRoles() == null) {
                    utilisateur.setRoles(new java.util.ArrayList<>());
                }
                utilisateur.getRoles().add(clientRole);
                utilisateurRepository.save(utilisateur);
       }
}
@Transactional
public ReservationResponseDTO updateReservationStatus(Long reservationId, String nouveauStatutStr) {

    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

    Utilisateurs utilisateur = reservation.getUtilisateur();

    // 🔴 Ancien statut
    StatutDeReservation ancienStatut = reservation.getStatut();

    // Conversion String → Enum
    StatutDeReservation nouveauStatut;
    try {
        nouveauStatut = StatutDeReservation.valueOf(nouveauStatutStr.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new RuntimeException("Statut invalide");
    }

    // Si le statut est identique → rien à faire
    if (ancienStatut == nouveauStatut) {
        return mapToDto(reservation);
    }

    // Mise à jour
    reservation.setStatut(nouveauStatut);
    reservationRepository.save(reservation);

    // 📧 Envoi email selon le statut
    emailService.envoyerEmailChangementStatut(utilisateur, reservation, ancienStatut);

    return mapToDto(reservation);
}

    //@Transactional

// public ReservationResponseDTO updateReservationStatus(Long reservationId, String nouveauStatutStr) {
//     // Récupérer la réservation
//     Reservation reservation = reservationRepository.findById(reservationId)
//             .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

//     // Récupérer l'utilisateur lié à la réservation
//     Utilisateurs utilisateur = reservation.getUtilisateur();

//     // Convertir le String en enum
//     StatutDeReservation nouveauStatut;
//     try {
//         nouveauStatut = StatutDeReservation.valueOf(nouveauStatutStr.toUpperCase());
//     } catch (IllegalArgumentException e) {
//         throw new RuntimeException("Statut invalide");
//     }

//     // Mettre à jour le statut
//     reservation.setStatut(nouveauStatut);
//     reservationRepository.save(reservation);

//     // Retourner le DTO
//     return ReservationResponseDTO.builder()
//             .id(reservation.getId())
//             .dateDebut(reservation.getDateDebut())
//             .dateFin(reservation.getDateFin())
//             .montant(reservation.getMontant())
//             .appartementNom(reservation.getAppartement().getNom())
//             .appartementAdresse(reservation.getAppartement().getAdresse())
//             .utilisateurNom(utilisateur.getNom())
//             .utilisateurPrenoms(utilisateur.getPrenoms())
//             .statut(reservation.getStatut().name())
//             .build();
//         }

private ReservationResponseDTO mapToDto(Reservation reservation) {
    Utilisateurs utilisateur = reservation.getUtilisateur();

    return ReservationResponseDTO.builder()
            .id(reservation.getId())
            .dateDebut(reservation.getDateDebut())
            .dateFin(reservation.getDateFin())
            .montant(reservation.getMontant())
            .appartementNom(reservation.getAppartement().getNom())
            .appartementAdresse(reservation.getAppartement().getAdresse())
            .utilisateurNom(utilisateur.getNom())
            .utilisateurPrenoms(utilisateur.getPrenoms())
            .statut(reservation.getStatut().name())
            .build();
}



@Transactional
public ReservationResponseVehi createReservationVehicule(ReservationRequestVehi request) {
    // 🔐 Récupérer le username depuis le token
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // 👤 Récupérer l'utilisateur connecté
    Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ensureClientRole(utilisateur);

    // 🚗 Récupérer le véhicule
    Vehicules vehicule = vehiculeRepository.findById(request.getVehiculeId())
            .orElseThrow(() -> new RuntimeException("Véhicule introuvable"));

    // 📅 Vérifier la validité des dates
    long jours = ChronoUnit.DAYS.between(request.getDateDebut(), request.getDateFin());
    if (jours <= 0) throw new RuntimeException("La date de fin doit être postérieure à la date de début");

    // 💰 Calcul du montant total
    double montant = jours * vehicule.getPrix();

    // 🔎 Vérifier les réservations existantes confirmées
    List<Reservation> reservations = reservationRepository
            .findByVehiculeAndStatut(vehicule, StatutDeReservation.CONFIRMEE)
            .orElse(Collections.emptyList());

    boolean dejaReserve = reservations.stream().anyMatch(r ->
            !(request.getDateFin().isBefore(r.getDateDebut()) ||
              request.getDateDebut().isAfter(r.getDateFin()))
    );

    if (dejaReserve) {
        throw new RuntimeException("Véhicule déjà réservé à ces dates !");
    }

    // 🏗️ Créer et sauvegarder la réservation
    Reservation reservation = Reservation.builder()
            .dateDebut(request.getDateDebut())
            .dateFin(request.getDateFin())
            .type(TypeDeRervation.VEHICULE)
            .statut(StatutDeReservation.EN_ATTENTE)
            .vehicule(vehicule)
            .montant(montant)
            .utilisateur(utilisateur)
            .build();

    Reservation savedReservation = reservationRepository.save(reservation);

    // 📧 Email + facture PDF spécifique véhicule
    emailService.envoyerEmailAvecFactureVehicule(utilisateur, savedReservation);

    // 🎁 Retourner le DTO de réponse
    return ReservationResponseVehi.builder()
            .id(savedReservation.getId())
            .dateDebut(savedReservation.getDateDebut())
            .dateFin(savedReservation.getDateFin())
            .vehiculeMarque(savedReservation.getVehicule().getMarque())
            .vehiculeImmatriculation(savedReservation.getVehicule().getImmatriculation())
            .utilisateurNom(savedReservation.getUtilisateur().getNom())
            .utilisateurPrenoms(savedReservation.getUtilisateur().getPrenoms())
            .statut(savedReservation.getStatut().name())
            .montant(savedReservation.getMontant())
            .build();
}

@Transactional
public ReservationResponseVehi updateReservationStatutVehi(Long reservartionId, String nouveauStatutStr){
        // Recuperer la reservation
        Reservation reservation = reservationRepository.findById(reservartionId)
                .orElseThrow(() -> new RuntimeException("Reservation non trouver"));

        // Récupérer l'utilisateur lié à la réservation
        Utilisateurs utilisateur = reservation.getUtilisateur();

        // 🔴 Ancien statut
        StatutDeReservation ancienStatut = reservation.getStatut();

        // Convertir le String en enum
        StatutDeReservation nouveau;
        try{
                nouveau= StatutDeReservation.valueOf(nouveauStatutStr.toUpperCase());

        } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide");
        }

        // Si le statut est identique → rien à faire
        if (ancienStatut == nouveau) {
            return ReservationResponseVehi.builder()
                    .id(reservation.getId())
                    .dateDebut(reservation.getDateDebut())
                    .dateFin(reservation.getDateFin())
                    .vehiculeMarque(reservation.getVehicule().getMarque())
                    .vehiculeImmatriculation(reservation.getVehicule().getImmatriculation())
                    .utilisateurNom(utilisateur.getNom())
                    .utilisateurPrenoms(utilisateur.getPrenoms())
                    .statut(reservation.getStatut().name())
                    .montant(reservation.getMontant())
                    .build();
        }

        // mettre a jour le statut
        reservation.setStatut(nouveau);
        reservationRepository.save(reservation);

        // 📧 Envoi email spécifique véhicule selon le statut
        emailService.envoyerEmailChangementStatutVehicule(utilisateur, reservation, ancienStatut);

        // retourner le DTO
        return ReservationResponseVehi.builder()
                        .id(reservation.getId())
                        .dateDebut(reservation.getDateDebut())
                        .dateFin(reservation.getDateFin())
                        .vehiculeMarque(reservation.getVehicule().getMarque())
                        .vehiculeImmatriculation(reservation.getVehicule().getImmatriculation())
                        .utilisateurNom(utilisateur.getNom())
                        .utilisateurPrenoms(utilisateur.getPrenoms())
                        .statut(reservation.getStatut().name())
                        .montant(reservation.getMontant())
                        .build();
                        
        }



        public List<ReservationResponseDTO> getReservationsByAppartement(Long appartementId) {
        return reservationRepository.findByAppartementId(appartementId)
                .stream()
                .map(reservation -> new ReservationResponseDTO(
                        reservation.getId(),
                        reservation.getDateDebut(),
                        reservation.getDateFin(),
                        reservation.getMontant(),
                        reservation.getAppartement() != null ? reservation.getAppartement().getNom() : null,
                        reservation.getAppartement() != null ? reservation.getAppartement().getAdresse() : null,
                        reservation.getUtilisateur() != null ? reservation.getUtilisateur().getNom() : null,
                        reservation.getUtilisateur() != null ? reservation.getUtilisateur().getPrenoms() : null,
                        reservation.getStatut() != null ? reservation.getStatut().name() : null
                ))
                .collect(Collectors.toList());
    }



        /**
         * Methode pour recuperer toutes les reservations
         */

       public List<ReservationResponseDTO> getAllReservations() {
    List<Reservation> reservations = reservationRepository.findAll();

    return reservations.stream()
            .map(r -> new ReservationResponseDTO(
                    r.getId(),
                    r.getDateDebut(),
                    r.getDateFin(),
                    r.getMontant(),
                    r.getAppartement() != null ? r.getAppartement().getNom() : null,
                    r.getAppartement() != null ? r.getAppartement().getAdresse() : null,
                    r.getUtilisateur() != null ? r.getUtilisateur().getNom() : null,
                    r.getUtilisateur() != null ? r.getUtilisateur().getPrenoms() : null,
                    r.getStatut() != null ? r.getStatut().name() : null
            ))
            .collect(Collectors.toList());
        }


        @Transactional
        public void deleteReservation(Long reservationId) {
    // Vérifier si la réservation existe
                Reservation reservation = reservationRepository.findById(reservationId)
                        .orElseThrow(() -> new RuntimeException("Réservation non trouvée avec l'id : " + reservationId));

    // Supprimer la réservation
                reservationRepository.delete(reservation);
}

        /**
         * methodes qui permet d'afficher uniquement que les Vehicules
         */

        public List<ReservationResponseVehi> getReservationsVehicules() {
                return reservationRepository.findByVehiculeIsNotNull()
                        .stream()
                        .map(res -> ReservationResponseVehi.builder()
                                .id(res.getId())
                                .dateDebut(res.getDateDebut())
                                .dateFin(res.getDateFin())
                                .vehiculeMarque(res.getVehicule().getMarque())
                                .vehiculeImmatriculation(res.getVehicule().getImmatriculation())
                                .utilisateurNom(res.getUtilisateur().getNom())
                                .utilisateurPrenoms(res.getUtilisateur().getPrenoms())
                                .statut(res.getStatut().name())
                                .montant(res.getMontant())
                                .build()
                        ).collect(Collectors.toList());
        }


        /**
         * Reservation liers Aux appartements
         
         */

        public List<ReservationResponseDTO> getReservationsAppartements() {
                return reservationRepository.findByAppartementIsNotNull()
                        .stream()
                        .map(res -> ReservationResponseDTO.builder()
                                .id(res.getId())
                                .dateDebut(res.getDateDebut())
                                .dateFin(res.getDateFin())
                                .appartementNom(res.getAppartement().getNom())
                                .appartementAdresse(res.getAppartement().getAdresse())
                                .utilisateurNom(res.getUtilisateur().getNom())
                                .utilisateurPrenoms(res.getUtilisateur().getPrenoms())
                                .montant(res.getMontant())
                                .statut(res.getStatut().name())
                                .build()
                        ).collect(Collectors.toList());
                }

        /**
         * Récupère toutes les réservations d'un propriétaire spécifique
         * @param proprietaireId L'ID du propriétaire
         * @return Liste des réservations du propriétaire
         */
        public List<ReservationResponseDTO> getReservationsByProprietaire(Long proprietaireId) {
            System.out.println("Récupération des réservations pour le propriétaire ID: " + proprietaireId);
            
            //List<Reservation> reservations = reservationRepository.findByAppartementProprietaireIdOrVehiculeProprietaireId(proprietaireId, proprietaireId);
            List<Reservation> reservations = reservationRepository.findByAppartementProprietaireId(proprietaireId);
            System.out.println("Nombre de réservations trouvées pour le propriétaire " + proprietaireId + ": " + reservations.size());
            
            return reservations.stream()
                    .map(reservation -> {
                        try {
                            return convertToReservationDTO(reservation);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la conversion de la réservation ID " + reservation.getId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        }

        /**
         * Récupère les réservations du propriétaire connecté
         * @return Liste des réservations du propriétaire connecté
         */
        public List<ReservationResponseDTO> getReservationsByCurrentUser() {
            // 🔐 Récupérer l'utilisateur connecté via le token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            System.out.println("Récupération des réservations pour l'utilisateur connecté: " + username);

            // 👤 Récupérer l'utilisateur dans la base
            Utilisateurs proprietaire = utilisateurRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            return getReservationsByProprietaire(proprietaire.getId());
        }

        /**
         * Convertit une entité Reservation en DTO
         */
        private ReservationResponseDTO convertToReservationDTO(Reservation reservation) {
            String appartementNom = null;
            String appartementAdresse = null;
            
            if (reservation.getAppartement() != null) {
                appartementNom = reservation.getAppartement().getNom();
                appartementAdresse = reservation.getAppartement().getAdresse();
            }
            
            return ReservationResponseDTO.builder()
                    .id(reservation.getId())
                    .dateDebut(reservation.getDateDebut())
                    .dateFin(reservation.getDateFin())
                    .montant(reservation.getMontant())
                    .appartementNom(appartementNom)
                    .appartementAdresse(appartementAdresse)
                    .utilisateurNom(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getNom() : null)
                    .utilisateurPrenoms(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getPrenoms() : null)
                    .statut(reservation.getStatut() != null ? reservation.getStatut().name() : null)
                    .build();
        }

        /**
         * Récupère toutes les réservations de véhicules d'un propriétaire spécifique
         * @param proprietaireId L'ID du propriétaire
         * @return Liste des réservations de véhicules du propriétaire
         */
        public List<ReservationResponseVehi> getReservationsVehiculesByProprietaire(Long proprietaireId) {
            System.out.println("Récupération des réservations de véhicules pour le propriétaire ID: " + proprietaireId);
            
            List<Reservation> reservations = reservationRepository.findByVehiculeProprietaireId(proprietaireId);
            System.out.println("Nombre de réservations de véhicules trouvées pour le propriétaire " + proprietaireId + ": " + reservations.size());
            
            return reservations.stream()
                    .filter(reservation -> reservation.getVehicule() != null) // S'assurer que c'est bien une réservation de véhicule
                    .map(reservation -> ReservationResponseVehi.builder()
                            .id(reservation.getId())
                            .dateDebut(reservation.getDateDebut())
                            .dateFin(reservation.getDateFin())
                            .vehiculeMarque(reservation.getVehicule().getMarque())
                            .vehiculeImmatriculation(reservation.getVehicule().getImmatriculation())
                            .utilisateurNom(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getNom() : null)
                            .utilisateurPrenoms(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getPrenoms() : null)
                            .statut(reservation.getStatut() != null ? reservation.getStatut().name() : null)
                            .montant(reservation.getMontant())
                            .build())
                    .collect(Collectors.toList());
        }

        /**
         * Récupère les réservations de véhicules du propriétaire connecté
         * @return Liste des réservations de véhicules du propriétaire connecté
         */
        public List<ReservationResponseVehi> getReservationsVehiculesByCurrentUserP() {
            // 🔐 Récupérer l'utilisateur connecté via le token
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           String username = authentication.getName();
           System.out.println("Récupération des réservations de véhicules pour l'utilisateur connecté: " + username);

            // 👤 Récupérer l'utilisateur dans la base
           Utilisateurs proprietaire = utilisateurRepository.findByEmail(username)
                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            return getReservationsVehiculesByProprietaire(proprietaire.getId());
       }

        // 🔹 Récupérer l'utilisateur connecté
        private Utilisateurs getCurrentUser() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            return utilisateurRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        }

    // 🔹 Réservations véhicules du user connecté
    public List<ReservationResponseVehi> getReservationsVehiculesByCurrentUser() {
        Utilisateurs user = getCurrentUser();
        return reservationRepository.findByUtilisateur_IdAndVehiculeIsNotNull(user.getId())
                .stream()
                .map(res -> ReservationResponseVehi.builder()
                        .id(res.getId())
                        .dateDebut(res.getDateDebut())
                        .dateFin(res.getDateFin())
                        .vehiculeMarque(res.getVehicule() != null ? res.getVehicule().getMarque() : null)
                        .vehiculeImmatriculation(res.getVehicule() != null ? res.getVehicule().getImmatriculation() : null)
                        .utilisateurNom(user.getNom())
                        .utilisateurPrenoms(user.getPrenoms())
                        .statut(res.getStatut() != null ? res.getStatut().name() : null)
                        .montant(res.getMontant())
                        .build())
                .collect(Collectors.toList());
    }

    // 🔹 Réservations véhicules par utilisateur
    public List<ReservationResponseVehi> getReservationsVehiculesByUser(Long utilisateurId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        List<Reservation> reservations = reservationRepository.findByUtilisateur_IdAndVehiculeIsNotNull(utilisateurId);

        return reservations.stream()
                .map(reservation -> ReservationResponseVehi.builder()
                        .id(reservation.getId())
                        .dateDebut(reservation.getDateDebut())
                        .dateFin(reservation.getDateFin())
                        .vehiculeMarque(reservation.getVehicule() != null ? reservation.getVehicule().getMarque() : null)
                        .vehiculeImmatriculation(reservation.getVehicule() != null ? reservation.getVehicule().getImmatriculation() : null)
                        .utilisateurNom(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getNom() : null)
                        .utilisateurPrenoms(reservation.getUtilisateur() != null ? reservation.getUtilisateur().getPrenoms() : null)
                        .statut(reservation.getStatut() != null ? reservation.getStatut().name() : null)
                        .montant(reservation.getMontant())
                        .build())
                .collect(Collectors.toList());
    }

    // 🔹 Réservations appartements du user connecté
    public List<ReservationResponseDTO> getReservationsAppartementsByCurrentUser() {
        Utilisateurs user = getCurrentUser();
        return reservationRepository.findByUtilisateur_IdAndAppartementIsNotNull(user.getId())
                .stream()
                .map(res -> ReservationResponseDTO.builder()
                        .id(res.getId())
                        .dateDebut(res.getDateDebut())
                        .dateFin(res.getDateFin())
                        .montant(res.getMontant())
                        .appartementNom(res.getAppartement() != null ? res.getAppartement().getNom() : null)
                        .appartementAdresse(res.getAppartement() != null ? res.getAppartement().getAdresse() : null)
                        .utilisateurNom(user.getNom())
                        .utilisateurPrenoms(user.getPrenoms())
                        .statut(res.getStatut() != null ? res.getStatut().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // 🔹 Réservations appartements par utilisateur (ID)
    public List<ReservationResponseDTO> getReservationsAppartementsByUser(Long utilisateurId) {
        return reservationRepository.findByUtilisateur_IdAndAppartementIsNotNull(utilisateurId)
                .stream()
                .map(res -> ReservationResponseDTO.builder()
                        .id(res.getId())
                        .dateDebut(res.getDateDebut())
                        .dateFin(res.getDateFin())
                        .montant(res.getMontant())
                        .appartementNom(res.getAppartement() != null ? res.getAppartement().getNom() : null)
                        .appartementAdresse(res.getAppartement() != null ? res.getAppartement().getAdresse() : null)
                        .utilisateurNom(res.getUtilisateur() != null ? res.getUtilisateur().getNom() : null)
                        .utilisateurPrenoms(res.getUtilisateur() != null ? res.getUtilisateur().getPrenoms() : null)
                        .statut(res.getStatut() != null ? res.getStatut().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si une réservation peut être annulée (dans les 24h après sa création)
     * @param reservationId L'ID de la réservation
     * @return true si l'annulation est possible, false sinon
     */
    public boolean canCancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        // Vérifier si la réservation est dans un statut annulable
        if (reservation.getStatut() == StatutDeReservation.ANNULEE || 
            reservation.getStatut() == StatutDeReservation.TERMINER) {
            return false;
        }
        
        // Vérifier si moins de 24h se sont écoulées depuis la création
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = reservation.getCreatedAt();
        long heuresDiff = ChronoUnit.HOURS.between(createdAt, now);
        
        return heuresDiff < 24;
    }

    /**
     * Annule une réservation si elle est éligible (créée il y a moins de 24h)
     * @param reservationId L'ID de la réservation à annuler
     * @return La réservation annulée
     */
    @Transactional
    public ReservationResponseDTO cancelReservation(Long reservationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        // Vérifier que l'utilisateur est bien le propriétaire de la réservation
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette réservation");
        }
        
        // Vérifier si la réservation est déjà annulée ou terminée
        if (reservation.getStatut() == StatutDeReservation.ANNULEE) {
            throw new RuntimeException("Cette réservation est déjà annulée");
        }
        
        if (reservation.getStatut() == StatutDeReservation.TERMINER) {
            throw new RuntimeException("Cette réservation est terminée et ne peut plus être annulée");
        }
        
        // Vérifier la limite de 24h
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = reservation.getCreatedAt();
        long heuresDiff = ChronoUnit.HOURS.between(createdAt, now);
        
        if (heuresDiff >= 24) {
            throw new RuntimeException("Le délai d'annulation de 24h est dépassé. Vous ne pouvez plus annuler cette réservation.");
        }
        
        // Conserver l'ancien statut pour l'email
        StatutDeReservation ancienStatut = reservation.getStatut();
        
        // Annuler la réservation
        reservation.setStatut(StatutDeReservation.ANNULEE);
        reservationRepository.save(reservation);
        
        // Envoyer un email de confirmation d'annulation
        emailService.envoyerEmailChangementStatut(utilisateur, reservation, ancienStatut);
        
        return mapToDto(reservation);
    }

    /**
     * Vérifie si une réservation peut être annulée pour l'utilisateur connecté
     * @param reservationId L'ID de la réservation
     * @return Objet contenant les infos sur la possibilité d'annulation
     */
    public boolean canCurrentUserCancelReservation(Long reservationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        // Vérifier que l'utilisateur est bien le propriétaire
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId())) {
            return false;
        }
        
        return canCancelReservation(reservationId);
    }

    /**
     * Obtient les informations détaillées sur la possibilité d'annulation d'une réservation
     * @param reservationId L'ID de la réservation
     * @return DTO avec les détails sur la possibilité d'annulation
     */
    public CancellationInfoDTO getCancellationInfo(Long reservationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        CancellationInfoDTO.CancellationInfoDTOBuilder builder = CancellationInfoDTO.builder()
                .reservationId(reservationId);
        
        // Vérifier que l'utilisateur est bien le propriétaire
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId())) {
            return builder
                    .canCancel(false)
                    .message("Vous n'êtes pas autorisé à annuler cette réservation")
                    .hoursRemaining(0L)
                    .build();
        }
        
        // Vérifier le statut
        if (reservation.getStatut() == StatutDeReservation.ANNULEE) {
            return builder
                    .canCancel(false)
                    .message("Cette réservation est déjà annulée")
                    .hoursRemaining(0L)
                    .build();
        }
        
        if (reservation.getStatut() == StatutDeReservation.TERMINER) {
            return builder
                    .canCancel(false)
                    .message("Cette réservation est terminée et ne peut plus être annulée")
                    .hoursRemaining(0L)
                    .build();
        }
        
        // Calculer le temps restant
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = reservation.getCreatedAt();
        long heuresDiff = ChronoUnit.HOURS.between(createdAt, now);
        long heuresRestantes = 24 - heuresDiff;
        
        if (heuresDiff >= 24) {
            return builder
                    .canCancel(false)
                    .message("Le délai d'annulation de 24h est dépassé")
                    .hoursRemaining(0L)
                    .build();
        }
        
        return builder
                .canCancel(true)
                .message("Vous pouvez annuler cette réservation")
                .hoursRemaining(heuresRestantes)
                .build();
    }

    /**
     * Annule une réservation de véhicule si elle est éligible (créée il y a moins de 24h)
     * @param reservationId L'ID de la réservation à annuler
     * @return La réservation annulée
     */
    @Transactional
    public ReservationResponseVehi cancelReservationVehicule(Long reservationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        // Vérifier que c'est bien une réservation de véhicule
        if (reservation.getVehicule() == null) {
            throw new RuntimeException("Cette réservation n'est pas une réservation de véhicule");
        }
        
        // Vérifier que l'utilisateur est bien le propriétaire de la réservation
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette réservation");
        }
        
        // Vérifier si la réservation est déjà annulée ou terminée
        if (reservation.getStatut() == StatutDeReservation.ANNULEE) {
            throw new RuntimeException("Cette réservation est déjà annulée");
        }
        
        if (reservation.getStatut() == StatutDeReservation.TERMINER) {
            throw new RuntimeException("Cette réservation est terminée et ne peut plus être annulée");
        }
        
        // Vérifier la limite de 24h
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = reservation.getCreatedAt();
        long heuresDiff = ChronoUnit.HOURS.between(createdAt, now);
        
        if (heuresDiff >= 24) {
            throw new RuntimeException("Le délai d'annulation de 24h est dépassé. Vous ne pouvez plus annuler cette réservation.");
        }
        
        // Conserver l'ancien statut pour l'email
        StatutDeReservation ancienStatut = reservation.getStatut();
        
        // Annuler la réservation
        reservation.setStatut(StatutDeReservation.ANNULEE);
        reservationRepository.save(reservation);
        
        // Envoyer un email de confirmation d'annulation
        emailService.envoyerEmailChangementStatut(utilisateur, reservation, ancienStatut);
        
        return ReservationResponseVehi.builder()
                .id(reservation.getId())
                .dateDebut(reservation.getDateDebut())
                .dateFin(reservation.getDateFin())
                .vehiculeMarque(reservation.getVehicule().getMarque())
                .vehiculeImmatriculation(reservation.getVehicule().getImmatriculation())
                .utilisateurNom(reservation.getUtilisateur().getNom())
                .utilisateurPrenoms(reservation.getUtilisateur().getPrenoms())
                .statut(reservation.getStatut().name())
                .montant(reservation.getMontant())
                .build();
    }

    /**
     * Vérifie si une réservation de véhicule peut être annulée pour l'utilisateur connecté
     * @param reservationId L'ID de la réservation
     * @return true si l'annulation est possible
     */
    public boolean canCurrentUserCancelReservationVehicule(Long reservationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        // Vérifier que c'est une réservation de véhicule
        if (reservation.getVehicule() == null) {
            return false;
        }
        
        // Vérifier que l'utilisateur est bien le propriétaire
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId())) {
            return false;
        }
        
        return canCancelReservation(reservationId);
    }

    /**
     * Obtient les informations détaillées sur la possibilité d'annulation d'une réservation de véhicule
     * @param reservationId L'ID de la réservation
     * @return DTO avec les détails sur la possibilité d'annulation
     */
    public CancellationInfoDTO getCancellationInfoVehicule(Long reservationId) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        CancellationInfoDTO.CancellationInfoDTOBuilder builder = CancellationInfoDTO.builder()
                .reservationId(reservationId);
        
        // Vérifier que c'est une réservation de véhicule
        if (reservation.getVehicule() == null) {
            return builder
                    .canCancel(false)
                    .message("Cette réservation n'est pas une réservation de véhicule")
                    .hoursRemaining(0L)
                    .build();
        }
        
        // Vérifier que l'utilisateur est bien le propriétaire
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId())) {
            return builder
                    .canCancel(false)
                    .message("Vous n'êtes pas autorisé à annuler cette réservation")
                    .hoursRemaining(0L)
                    .build();
        }
        
        // Vérifier le statut
        if (reservation.getStatut() == StatutDeReservation.ANNULEE) {
            return builder
                    .canCancel(false)
                    .message("Cette réservation est déjà annulée")
                    .hoursRemaining(0L)
                    .build();
        }
        
        if (reservation.getStatut() == StatutDeReservation.TERMINER) {
            return builder
                    .canCancel(false)
                    .message("Cette réservation est terminée et ne peut plus être annulée")
                    .hoursRemaining(0L)
                    .build();
        }
        
        // Calculer le temps restant
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = reservation.getCreatedAt();
        long heuresDiff = ChronoUnit.HOURS.between(createdAt, now);
        long heuresRestantes = 24 - heuresDiff;
        
        if (heuresDiff >= 24) {
            return builder
                    .canCancel(false)
                    .message("Le délai d'annulation de 24h est dépassé")
                    .hoursRemaining(0L)
                    .build();
        }
        
        return builder
                .canCancel(true)
                .message("Vous pouvez annuler cette réservation de véhicule")
                .hoursRemaining(heuresRestantes)
                .build();
    }

    /**
     * Met à jour une réservation existante (dates, montant recalculé automatiquement)
     */
    @Transactional
    public ReservationResponseDTO updateReservation(Long reservationId, ReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée avec l'ID: " + reservationId));

        boolean datesModifiees = false;

        // Mettre à jour les dates si elles sont fournies
        if (request.getDateDebut() != null) {
            reservation.setDateDebut(request.getDateDebut());
            datesModifiees = true;
        }
        if (request.getDateFin() != null) {
            reservation.setDateFin(request.getDateFin());
            datesModifiees = true;
        }
        
        // Recalculer le montant si les dates ont été modifiées
        if (datesModifiees && reservation.getAppartement() != null) {
            long jours = ChronoUnit.DAYS.between(reservation.getDateDebut(), reservation.getDateFin());
            if (jours <= 0) {
                throw new RuntimeException("La date de fin doit être postérieure à la date de début");
            }
            double nouveauMontant = jours * reservation.getAppartement().getPrix();
            reservation.setMontant(nouveauMontant);
        }

        // Sauvegarder les modifications
        Reservation updatedReservation = reservationRepository.save(reservation);
        
        return mapToDto(updatedReservation);
    }

    /**
     * Met à jour une réservation de véhicule existante (dates uniquement, montant recalculé automatiquement)
     * Note : Le véhicule ne peut pas être modifié
     */
    @Transactional
    public ReservationResponseVehi updateReservationVehicule(Long reservationId, ReservationRequestVehi request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée avec l'ID: " + reservationId));

        // Vérifier que la réservation a un véhicule
        if (reservation.getVehicule() == null) {
            throw new RuntimeException("Cette réservation n'est pas liée à un véhicule");
        }

        boolean datesModifiees = false;

        // Mettre à jour les dates si elles sont fournies
        if (request.getDateDebut() != null) {
            reservation.setDateDebut(request.getDateDebut());
            datesModifiees = true;
        }
        if (request.getDateFin() != null) {
            reservation.setDateFin(request.getDateFin());
            datesModifiees = true;
        }
        
        // Recalculer le montant si les dates ont été modifiées
        if (datesModifiees) {
            long jours = ChronoUnit.DAYS.between(reservation.getDateDebut(), reservation.getDateFin());
            if (jours <= 0) {
                throw new RuntimeException("La date de fin doit être postérieure à la date de début");
            }
            double nouveauMontant = jours * reservation.getVehicule().getPrix();
            reservation.setMontant(nouveauMontant);
        }

        // Sauvegarder les modifications
        Reservation updatedReservation = reservationRepository.save(reservation);
        
        // Retourner le DTO
        return ReservationResponseVehi.builder()
                .id(updatedReservation.getId())
                .dateDebut(updatedReservation.getDateDebut())
                .dateFin(updatedReservation.getDateFin())
                .vehiculeMarque(updatedReservation.getVehicule().getMarque())
                .vehiculeImmatriculation(updatedReservation.getVehicule().getImmatriculation())
                .utilisateurNom(updatedReservation.getUtilisateur().getNom())
                .utilisateurPrenoms(updatedReservation.getUtilisateur().getPrenoms())
                .statut(updatedReservation.getStatut().name())
                .montant(updatedReservation.getMontant())
                .build();
    }


}