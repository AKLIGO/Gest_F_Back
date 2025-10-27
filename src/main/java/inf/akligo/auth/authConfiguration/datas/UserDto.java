package inf.akligo.auth.authConfiguration.datas;

import inf.akligo.auth.authConfiguration.entity.Roles;

import java.util.List;

public class UserDto {
    private Long id;
    private String nom;
    private String prenoms;
    private String email;
    private List<Roles> roles;

    // Constructeur
    public UserDto(Long id, String nom, String prenoms, String email, List<Roles> roles) {
        this.id = id;
        this.nom = nom;
        this.prenoms = prenoms;
        this.email = email;
        this.roles = roles;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public String getEmail() {
        return email;
    }

    public List<Roles> getRoles() {
        return roles;
    }

    // Setters si nécessaire
    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoles(List<Roles> roles) {
        this.roles = roles;
    }
}