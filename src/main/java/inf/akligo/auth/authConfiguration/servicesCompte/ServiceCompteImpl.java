package inf.akligo.auth.authConfiguration.servicesCompte;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.entity.Roles;
import inf.akligo.auth.authConfiguration.repository.TokenRepository;
import inf.akligo.auth.authConfiguration.entity.Token;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import inf.akligo.auth.authConfiguration.datas.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.List;
import java.util.Collections;
import java.time.LocalDateTime;
import java.security.SecureRandom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Lazy;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import inf.akligo.auth.authConfiguration.repository.RoleRepository;
import inf.akligo.auth.securityConfig.serviceEmail.EmailService;
import inf.akligo.auth.securityConfig.serviceEmail.EmailTemplateName;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import inf.akligo.auth.securityConfig.security.JwtService;
import java.util.HashMap;
import inf.akligo.auth.authConfiguration.request.AuthenticationRequest;
import inf.akligo.auth.authConfiguration.request.AuthenticationResponse;

@Service
@Transactional
public class ServiceCompteImpl implements ServiceCompte, UserDetailsService {

    @Value("${mailing.frontend.activation-url}")
    private String activationUrl;

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ServiceCompteImpl(UtilisateurRepository utilisateurRepository, 
                             RoleRepository roleRepository,
                             TokenRepository tokenRepository,
                             @Lazy PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             @Lazy AuthenticationManager authenticationManager,
                             EmailService emailService) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.authenticationManager=authenticationManager;
        this.jwtService=jwtService;
    }

    @Override
    public Utilisateurs ajouterUtilisateur(Utilisateurs utilisateur) {
        Roles roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER non trouvé"));

        utilisateur.setRoles(Collections.singletonList(roleUser));
        utilisateur.setEnabled(false);
        utilisateur.setAccountLocked(false);
        utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));

        Utilisateurs savedUser = utilisateurRepository.save(utilisateur);

        // Envoi email dans try/catch
        sendValidationEmail(savedUser);

        return savedUser;
    }

    @Override
    public Roles ajouterRole(Roles role) {
        return roleRepository.save(role);
    }

    @Override
    public void ajouterRoleAUtilisateur(String email, String name) {
        Utilisateurs utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Roles role = roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role non trouvé"));

        utilisateur.getRoles().add(role);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateurs loadUserByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Override
    public List<Utilisateurs> listeUtilisateur() {
        return utilisateurRepository.findAll();
    }

    @Override
    public void supprimerUtilisateurParId(Long id) {
        Utilisateurs utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
        utilisateurRepository.delete(utilisateur);
    }

    @Override
    public Utilisateurs modifierUtilisateur(Utilisateurs utilisateurDetails, Long id) {
        Utilisateurs utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        utilisateur.setNom(utilisateurDetails.getNom());
        utilisateur.setPrenoms(utilisateurDetails.getPrenoms());
        utilisateur.setEmail(utilisateurDetails.getEmail());
        utilisateur.setTelephone(utilisateurDetails.getTelephone());
        utilisateur.setAdresse(utilisateurDetails.getAdresse());

        if (utilisateurDetails.getPassword() != null && !utilisateurDetails.getPassword().isBlank()) {
            utilisateur.setPassword(passwordEncoder.encode(utilisateurDetails.getPassword()));
        }

        utilisateur.setAccountLocked(utilisateurDetails.isAccountLocked());
        utilisateur.setEnabled(utilisateurDetails.isEnabled());

        return utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void sendValidationEmail(Utilisateurs utilisateur) {
        var newToken = generateAndSaveActivationToken(utilisateur);

        try {
            emailService.sendEmail(
                    utilisateur.getEmail(),
                    utilisateur.getFullName(),
                    EmailTemplateName.ACTIVATE_ACCOUNT,
                    newToken,
                    activationUrl,
                    
                    "Account activation"
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    public String generateAndSaveActivationToken(Utilisateurs utilisateur) {
        String generatedToken = generateActivationCode(6);

        Token token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .utilisateur(utilisateur)
                .build();

        tokenRepository.save(token);

        
        return generatedToken;
    }

    public String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var auth= authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((Utilisateurs)auth.getPrincipal());

        claims.put("fullName", user.getFullName());
        var jwtToken = jwtService.generateToken(claims,user);
         //recuperons l'entiter depuis la base de donnees


        return AuthenticationResponse.builder()
                        .token(jwtToken)
                        .user(user)
                        .build();
    }


     public void activationAccount(String token){
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invali Token"));

        if(LocalDateTime.now().isAfter(savedToken.getExpiration())){
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has Expired. A new token has been send ");
        }

        var user=utilisateurRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouver"));
        user.setEnabled(true);
        utilisateurRepository.save(user);
        savedToken.setValidateAt(LocalDateTime.now());
        tokenRepository.save(savedToken);        
                
    }


     /**
     * Récupère l'utilisateur courant à partir de l'authentication Spring Security
     */


    /**
     * Récupère l'utilisateur courant et le mappe vers un DTO
     */
    public Optional<UserDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }

        return utilisateurRepository.findByEmail(authentication.getName())
                .map(user -> new UserDto(
                        user.getId(),
                        user.getNom(),
                        user.getPrenoms(),
                        user.getEmail(),
                        user.getRoles()
                ));
    
}

}
