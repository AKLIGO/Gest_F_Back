
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
// import inf.akligo.auth.gestionDesBiens.requests.ReservationResponseVehi;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ServiceReservation{

    private final ReservationRepository reservationRepository;
    private final AppartementRepository appartementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final RoleRepository roleRepository;


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

        return reservationRepository.save(reservation);
    
    
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
    // Récupérer la réservation
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

    // Récupérer l'utilisateur lié à la réservation
    Utilisateurs utilisateur = reservation.getUtilisateur();

    // Convertir le String en enum
    StatutDeReservation nouveauStatut;
    try {
        nouveauStatut = StatutDeReservation.valueOf(nouveauStatutStr.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new RuntimeException("Statut invalide");
    }

    // Mettre à jour le statut
    reservation.setStatut(nouveauStatut);
    reservationRepository.save(reservation);

    // Retourner le DTO
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
            .build();
}

@Transactional
public ReservationResponseVehi updateReservationStatutVehi(Long reservartionId, String nouveauStatut){
        // Recuperer la reservation
        Reservation reservation = reservationRepository.findById(reservartionId)
                .orElseThrow(() -> new RuntimeException("Reservation non trouver"));

                    // Récupérer l'utilisateur lié à la réservation
        Utilisateurs utilisateur = reservation.getUtilisateur();

        // Convertir le String en enum

        StatutDeReservation nouveau;
        try{
                nouveau= StatutDeReservation.valueOf(nouveauStatut.toUpperCase());

        } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide");
    }
        // mettre a jour le statut

        reservation.setStatut(nouveau);
        reservationRepository.save(reservation);

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



}