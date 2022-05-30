package com.cebbus.notification;

import com.cebbus.properties.Mail;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static javax.mail.Message.RecipientType.TO;

@Slf4j
public class MailNotifier extends BaseNotifier {

    private final Mail mail;
    private final Session instance;

    public MailNotifier(Notifier notifier) {
        super(notifier);

        this.mail = PropertyReader.getMail();
        this.instance = Session.getInstance(this.mail.getProperties(), new MailAuthenticator());
    }

    @Override
    public void send(String message) {
        super.send(message);
        sendMail(message);
    }

    private void sendMail(String content) {
        try {
            Message message = new MimeMessage(this.instance);

            message.setFrom(new InternetAddress("cebbot@notifier.com"));
            message.setRecipients(TO, InternetAddress.parse(this.mail.getTo()));
            message.setSubject("Report from Cebbot!");

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(content, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
        }
    }

    private class MailAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mail.getUsername(), mail.getPassword());
        }
    }
}
