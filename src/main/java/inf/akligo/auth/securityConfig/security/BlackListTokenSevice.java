package inf.akligo.auth.securityConfig.security;

import org.springframework.stereotype.Service;
import inf.akligo.auth.securityConfig.security.JwtService;
import inf.akligo.auth.authConfiguration.repository.BlackListedTokenRepository;
import lombok.RequiredArgsConstructor;
import inf.akligo.auth.authConfiguration.entity.BlackListedToken;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class BlackListTokenSevice{
    private final BlackListedTokenRepository blackListedTokenRepository;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(BlackListTokenSevice.class);

    //Ajouter un token a la blackList

    @Transactional
    public void blackListToken(String token,String userEmail, String reason){

        try {
        // Extraction de la date d'expiration du token JWT
        Date expirationDate = jwtService.extractExpiration(token);
        LocalDateTime expiresAt = expirationDate.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();

        logger.info("Adding token to blacklist: {}, Expiration Date: {}", token, expiresAt);

        BlackListedToken blacklistToken = BlackListedToken.builder()
                            .token(token)
                            .userEmail(userEmail)
                            .expiresAt(expiresAt)
                            .blacklistedAt(LocalDateTime.now())
                            .reason(reason)
                            .build();

        blackListedTokenRepository.save(blacklistToken);
        logger.info("Token blacklisted for user: {}", userEmail);
    } catch (Exception e) {
        logger.error("Error while blacklisting token: {}", e.getMessage());
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        BlackListedToken blackListToken = BlackListedToken.builder()
                        .token(token)
                        .userEmail(userEmail)
                        .expiresAt(expiresAt)
                        .blacklistedAt(LocalDateTime.now())
                        .reason(reason + " (token invalid)")
                        .build();

        blackListedTokenRepository.save(blackListToken);
        logger.info("Token added to blacklist with default expiration for user: {}", userEmail);
    } 
    }

    


    public boolean isTokenBlackListed(String token) {
        return blackListedTokenRepository.findByToken(token).isPresent();
}


}