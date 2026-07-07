package ar.utn.ba.ddsi.mailing.adapters;

import ar.utn.ba.ddsi.mailing.models.entities.Email;

public interface IEmailSenderAdapter {
  void enviar(Email email);
}
