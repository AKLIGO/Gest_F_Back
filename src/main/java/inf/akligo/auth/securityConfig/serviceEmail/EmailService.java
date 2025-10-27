package inf.akligo.auth.securityConfig.serviceEmail;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.scheduling.annotation.Async;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailService{

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(
        String to,
        String username,
        EmailTemplateName emailTemplate,
        String activationCode,
        String confirmationUrl,
        String subject
    ) throws MessagingException {
            
        String templateName;
        if(emailTemplate == null){
            templateName = "confirm-email";
        } else {
            templateName = emailTemplate.getName();
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
            mimeMessage,
            true,
            StandardCharsets.UTF_8.name()
        );
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("koffitsejeromea@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);
        mailSender.send(mimeMessage);
    }

        @Async
    public void sendActivationEmail(String to, String activationCode, String username) throws MessagingException {
        String activationLink = "http://localhost:4200/activate-account?token=" + activationCode;

        sendEmail(
                to,
                username,
                null,                  // template par défaut
                activationCode,
                activationLink,
                "Activation de votre compte"
        );
    }
}