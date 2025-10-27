package inf.akligo.auth.gestionDesBiens.services.serviceCompteImpl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import inf.akligo.auth.securityConfig.security.JwtService;
import java.util.HashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.List;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.repository.UtilisateurRepository;
import inf.akligo.auth.gestionDesBiens.entity.Immeuble;
import inf.akligo.auth.gestionDesBiens.entity.Appartement;
import inf.akligo.auth.gestionDesBiens.repository.ImmeubleRepository;
import inf.akligo.auth.gestionDesBiens.repository.AppartementRepository;
import inf.akligo.auth.gestionDesBiens.services.serviceCompteImpl.ServiceImmeuble;



@Service
public class ServiceImmeubleIm implements ServiceImmeuble{
    private final UtilisateurRepository utilisateurRepository;
    private final ImmeubleRepository immeubleRepository;
    private final AppartementRepository appartementRepository;
    private final JwtService jwtService;

       public ServiceImmeubleIm(
            UtilisateurRepository utilisateurRepository,
            ImmeubleRepository immeubleRepository,
            AppartementRepository appartementRepository,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.immeubleRepository = immeubleRepository;
        this.appartementRepository = appartementRepository;
        this.jwtService = jwtService;
    }


    @Override
    public Immeuble addImmeuble(Immeuble immeuble) { 
        //recuperer l'utilisateur connecter a partir du contexte Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        Utilisateurs user = utilisateurRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("utilisateur non trouver"));

                    // associer le proprio
         immeuble.setUtilisateur(user);


        return immeubleRepository.save(immeuble);

}


    @Override
    public Immeuble updateImmeuble(Immeuble immeubleUpdate, Long id) { 
        Immeuble existant = immeubleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Immeuble nn trouver"));
        existant.setNbrAppartment(immeubleUpdate.getNbrAppartment());
        existant.setNbrEtage(immeubleUpdate.getNbrEtage());
        existant.setNom(immeubleUpdate.getNom());
        existant.setVille(immeubleUpdate.getVille());
        existant.setDescription(immeubleUpdate.getDescription());
        
        return immeubleRepository.save(existant);
     }


    @Override
    public void removeImmeuble(Long id) {
        immeubleRepository.deleteById(id);
    }


    @Override
    public List<Immeuble> listImmeuble() { 
        return immeubleRepository.findAll();
    }
    

    @Override
    public List<Appartement> listAppartement() { 
        return null;
     }


}