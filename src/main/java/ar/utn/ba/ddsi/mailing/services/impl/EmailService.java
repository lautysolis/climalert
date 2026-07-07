package ar.utn.ba.ddsi.mailing.services.impl;

import ar.utn.ba.ddsi.mailing.adapters.IEmailSenderAdapter;
import ar.utn.ba.ddsi.mailing.models.entities.Email;
import ar.utn.ba.ddsi.mailing.models.repositories.IEmailRepository;
import ar.utn.ba.ddsi.mailing.services.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailService implements IEmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final IEmailRepository emailRepository;
    private final IEmailSenderAdapter emailSenderAdapter;

    public EmailService(IEmailRepository emailRepository, IEmailSenderAdapter emailSenderAdapter) {
        this.emailRepository = emailRepository;
        this.emailSenderAdapter = emailSenderAdapter;
    }

    @Override
    public Email crearEmail(Email email) {
        return emailRepository.save(email);
    }

    @Override
    public List<Email> obtenerEmails(Boolean pendiente) {
        if (pendiente != null) {
            return emailRepository.findByEnviado(!pendiente);
        }
        return emailRepository.findAll();
    }

    @Override
    public void procesarPendientes() {
        List<Email> pendientes = emailRepository.findByEnviado(false);
        for (Email email : pendientes) {
            try {
                emailSenderAdapter.enviar(email);
                email.setEnviado(true);
            } catch (Exception e) {
                // Si falla el envio, el email queda pendiente y se reintenta
                // en la proxima corrida del scheduler.
                logger.error("Error al enviar email a {}: {}", email.getDestinatario(), e.getMessage());
            }
            emailRepository.save(email);
        }
    }

    @Override
    public void loguearEmailsPendientes() {
        List<Email> pendientes = obtenerEmails(true);
        logger.info("Emails pendientes de envío: {}", pendientes.size());
        pendientes.forEach(email ->
            logger.info("Email pendiente - ID: {}, Destinatario: {}, Asunto: {}",
                email.getId(),
                email.getDestinatario(),
                email.getAsunto())
        );
    }
}
