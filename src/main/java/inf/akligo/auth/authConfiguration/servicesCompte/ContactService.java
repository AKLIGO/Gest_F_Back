package inf.akligo.auth.authConfiguration.servicesCompte;
import inf.akligo.auth.authConfiguration.entity.ContactMessage;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import inf.akligo.auth.authConfiguration.repository.ContactRepository;
@Service
@RequiredArgsConstructor
public class ContactService{
    private final ContactRepository contactRepository;

    public ContactMessage saveMessage(ContactMessage message){
        return contactRepository.save(message);
    }
}