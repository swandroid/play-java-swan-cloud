package actuator;


import credentials.Gmail;
import interdroid.swancore.swansong.TriState;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;


/**
 * Created by goose on 16/06/16.
 */
public class SendEmail
{
    public static void sendEmail(String to, String expressionId, String expression, TriState newState) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Gmail.FROM_EMAIL, Gmail.FROM_PASSWORD);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Gmail.FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject("New Swan Sensor State");
            message.setText("Hello,\n" +
                    "\nExpression Id:"+expressionId+
                    "\nExpression:"+expression+
                    "\nNew State:"+newState+
                    "\n"+
                    "\nBest Regards,"+
                    "\nSWAN Team");

            Transport.send(message);

            System.out.println("Mail sent succesfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}