package com.employee.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger =
            LoggerFactory.getLogger(EmailService.class);

    public void sendNotification(String email,
                                 String subject,
                                 String body) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            logger.info("Email sent successfully to {}", email);

        } catch (Exception e) {

            logger.error(
                    "Failed to send email to {}",
                    email,
                    e);
        }
    }
}