package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.user.User
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import spock.lang.Specification
import spock.lang.Subject

class MainNotificationServiceSpec extends Specification {

    def mailSender = Mock(JavaMailSender)
    @Subject
    def mainNotificationService = new MainNotificationServiceImpl(mailSender)

    def "test sendNotificationToOwner"() {
        given:
        User owner = new User(email: 'owner@example.com')
        String message = 'Test Message'
        String subject = 'Test Subject'

        when:
        mainNotificationService.sendNotificationToUser(owner, message, subject)

        then:
        1 * mailSender.send({ SimpleMailMessage email ->
            email.to == ['owner@example.com']
            email.subject == 'Test Subject'
            email.text == 'Test Message'
        })
    }

}
