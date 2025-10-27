package inf.akligo.auth.authConfiguration.servicesCompte;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.authConfiguration.entity.Roles;

import java.util.List;

public interface ServiceCompte{
    Utilisateurs ajouterUtilisateur(Utilisateurs utilisateur);
    Roles ajouterRole(Roles role);

    void ajouterRoleAUtilisateur(String email,String name);

    //retourner un utilisateur a partir du email

    Utilisateurs loadUserByEmail(String email);

    // methode qui permet de retourner une liste d'utilisateur

    List<Utilisateurs> listeUtilisateur();

    // supprimer un utilisateur
    void supprimerUtilisateurParId(Long id);

    Utilisateurs modifierUtilisateur(Utilisateurs utilisateurDetails, Long id);




}