package com.reddit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.reddit.exception.RedditException;
import com.reddit.model.NotificationEmail;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j   //for logger
public class MailService {

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private MailContentBuilder mailContentBuilder;
	
	@Async //it will run in BG, not slowing down the overall timing.
	void sendMail(NotificationEmail notificationEmail) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom("262jaskaran@gmail.com");
			messageHelper.setTo(notificationEmail.getRecipient());
			messageHelper.setSubject(notificationEmail.getSubject());
			messageHelper.setText(mailContentBuilder.build(notificationEmail.getBody())); //html 
		};
		
		try {
			mailSender.send(messagePreparator);
			log.info("Activation email sent successfully!");
		} catch (MailException e) {
			throw new RedditException("Exception occurred when sending activation mail to: "
									  + notificationEmail.getRecipient());
		}
	}
}
