package com.medibook.service;
import org.springframework.beans.factory.ObjectProvider;import org.springframework.beans.factory.annotation.Value;import org.springframework.mail.*;import org.springframework.mail.javamail.*;import org.springframework.stereotype.Component;
@Component public class EmailNotifier {
 private final ObjectProvider<JavaMailSender> senders;private final boolean enabled;private final String host,from;
 public EmailNotifier(ObjectProvider<JavaMailSender> senders,@Value("${medibook.email.enabled:false}") boolean enabled,@Value("${spring.mail.host:}") String host,@Value("${medibook.email.from:no-reply@medibook.local}") String from){this.senders=senders;this.enabled=enabled;this.host=host;this.from=from;}
 public void send(String recipient,String subject,String body){if(!enabled||host.isBlank()||recipient==null||recipient.isBlank())return;JavaMailSender sender=senders.getIfAvailable();if(sender==null)return;SimpleMailMessage message=new SimpleMailMessage();message.setFrom(from);message.setTo(recipient);message.setSubject(subject);message.setText(body);try{sender.send(message);}catch(MailException ignored){/* Email is optional; keep appointment workflows available. */}}
}
