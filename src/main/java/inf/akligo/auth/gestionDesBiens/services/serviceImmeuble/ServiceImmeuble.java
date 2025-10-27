package inf.akligo.auth.gestionDesBiens.services.serviceCompteImpl;

import inf.akligo.auth.authConfiguration.entity.Utilisateurs;
import inf.akligo.auth.gestionDesBiens.entity.Immeuble;

import inf.akligo.auth.gestionDesBiens.entity.Appartement;

import java.util.List;


public interface ServiceImmeuble{

    public Immeuble addImmeuble(Immeuble immeuble);

    public Immeuble updateImmeuble(Immeuble immeubleUpdate,Long id);

    public void removeImmeuble(Long id);

    List<Immeuble> listImmeuble();

    List<Appartement> listAppartement();




}