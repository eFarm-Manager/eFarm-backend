package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository;
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class FinanceNotificationServiceSpec extends Specification {

    def mailSender = Mock(JavaMailSender)
    def userService = Mock(UserService)
    def financialCategoryRepository = Mock(FinancialCategoryRepository)
    def paymentStatusRepository = Mock(PaymentStatusRepository)
    def transactionRepository = Mock(TransactionRepository)

    @Subject
    FinanceNotificationService financeNotificationService = new FinanceNotificationService(
        mailSender: mailSender,
        userService: userService,
        financialCategoryRepository: financialCategoryRepository,
        paymentStatusRepository: paymentStatusRepository,
        transactionRepository: transactionRepository
    )

    def "test checkAndNotifyForPayment when payment date is 5 days away"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> today.plusDays(5)
        transaction.getTransactionName() >> "Test Transaction"
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
        }
        User user = Mock(User)
        user.getEmail() >> "test@example.com"
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        1 * mailSender.send(_ as SimpleMailMessage)
    }

    def "test checkAndNotifyForPayment when payment date is 1 day away"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> today.plusDays(1)
        transaction.getTransactionName() >> "Test Transaction"
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
        }
        User user = Mock(User)
        user.getEmail() >> "test@example.com"
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        1 * mailSender.send(_ as SimpleMailMessage)
    }

    def "test checkAndNotifyForPayment when payment date is not 5 or 1 day away"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> today.plusDays(3)
        transaction.getTransactionName() >> "Test Transaction"
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
        }
        User user = Mock(User)
        user.getEmail() >> "test@example.com"
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        0 * mailSender.send(_ as SimpleMailMessage)
    }

    def "test checkAndNotifyForPayment when transaction has no payment date"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> null
        transaction.getTransactionName() >> "Test Transaction"
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
        }
        User user = Mock(User)
        user.getEmail() >> "test@example.com"
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        0 * mailSender.send(_ as SimpleMailMessage)
    }

    def "should send notification to owner"() {
        given:
        User owner = new User(email: "owner@example.com")
        String message = "Test message"
        String subject = "Test subject"

        when:
        financeNotificationService.sendNotificationToOwner(owner, message, subject)

        then:
        1 * mailSender.send(_ as SimpleMailMessage) >> { SimpleMailMessage email ->
            assert email.to == ["owner@example.com"]
            assert email.subject == subject
            assert email.text == message
        }
    }

}
