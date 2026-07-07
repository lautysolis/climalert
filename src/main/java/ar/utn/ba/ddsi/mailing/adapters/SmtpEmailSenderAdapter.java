package ar.utn.ba.ddsi.mailing.adapters;

import ar.utn.ba.ddsi.mailing.models.entities.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSenderAdapter implements IEmailSenderAdapter {
  private static final Logger logger = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

  private final JavaMailSender mailSender;

  public SmtpEmailSenderAdapter(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void enviar(Email email) {
    SimpleMailMessage mensaje = new SimpleMailMessage();
    mensaje.setFrom(email.getRemitente());
    mensaje.setTo(email.getDestinatario());
    mensaje.setSubject(email.getAsunto());
    mensaje.setText(email.getContenido());

    mailSender.send(mensaje);
    logger.info("Correo enviado a {} (asunto: {})", email.getDestinatario(), email.getAsunto());
  }
}
