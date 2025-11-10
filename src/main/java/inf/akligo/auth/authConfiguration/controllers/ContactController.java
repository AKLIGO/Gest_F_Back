package inf.akligo.auth.authConfiguration.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import inf.akligo.auth.authConfiguration.entity.ContactMessage;
import inf.akligo.auth.authConfiguration.servicesCompte.ContactService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;


@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/contact")
@RestController
@RequiredArgsConstructor
public class ContactController{

       
    private final ContactService contactService;

    @PostMapping
    public ContactMessage sendMessage(@RequestBody ContactMessage message) {
        return contactService.saveMessage(message);
    }

}