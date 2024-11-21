package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MainNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendNotificationToUser(User user, String message, String subject) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}
