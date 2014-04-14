package models.util

import play.api.i18n.Messages
import play.api.Play.current
import com.ucheck.common.Email
import com.typesafe.plugin._

object EmailSender {
  def sendEmail(email: Email) = {
    val mail = use[MailerPlugin].email
    mail.setSubject(email.subject)
    mail.addRecipient(email.email)
    mail.addFrom(Messages("contact.request.email.from"))
    mail.send(email.body)
  }
}