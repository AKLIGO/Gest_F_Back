package inf.akligo.auth.authConfiguration.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;

import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;



@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "users")
public class Roles{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private List<Utilisateurs> users;

    
    
    public String getName() {
        return name;
    }

 


}