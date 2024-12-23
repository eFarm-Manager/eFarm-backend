package com.efarm.efarmbackend.service.finance

import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.finance.*
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository
import com.efarm.efarmbackend.repository.finance.TransactionRepository
import com.efarm.efarmbackend.service.MainNotificationServiceImpl
import com.efarm.efarmbackend.service.user.UserServiceImpl
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class FinanceNotificationServiceSpec extends Specification {

    def transactionRepository = Mock(TransactionRepository)
    def userService = Mock(UserServiceImpl)
    def financialCategoryRepository = Mock(FinancialCategoryRepository)
    def paymentStatusRepository = Mock(PaymentStatusRepository)
    def mainNotificationService = Mock(MainNotificationServiceImpl)

    @Subject
    FinanceNotificationServiceImpl financeNotificationService = new FinanceNotificationServiceImpl(
            transactionRepository,
            userService,
            financialCategoryRepository,
            paymentStatusRepository,
            mainNotificationService
    )

    def "should call checkAndNotifyForPayment for each transaction"() {
        given:
        LocalDate today = LocalDate.now()
        FinancialCategory financialCategory = new FinancialCategory(name: EFinancialCategory.EXPENSE)
        PaymentStatus paymentStatus = new PaymentStatus(name: EPaymentStatus.UNPAID)
        Farm farm = Mock(Farm)
        farm.id >> 1
        farm.isActive >> true

        Transaction transaction1 = Mock(Transaction)
        Transaction transaction2 = Mock(Transaction)

        transaction1.paymentDate >> today.plusDays(5)
        transaction2.paymentDate >> today.plusDays(1)

        transaction1.farm >> farm
        transaction2.farm >> farm

        financialCategoryRepository.findByName(EFinancialCategory.EXPENSE) >> financialCategory
        paymentStatusRepository.findByName(EPaymentStatus.UNPAID) >> paymentStatus
        transactionRepository.findByfinancialCategoryAndPaymentStatus(financialCategory, paymentStatus) >>
                [transaction1, transaction2]

        userService.getAllOwnersForFarm(1) >> [Mock(User) { getIsActive() >> true }]

        when:
        financeNotificationService.checkPaymentDueDateNotifications()

        then:
        1 * mainNotificationService.sendNotificationToUser(_ as User, _, 'Niedługo upływa termin płatności!')
        1 * mainNotificationService.sendNotificationToUser(_ as User, _, 'Niedługo upływa termin płatności!')
    }

    def "should checkAndNotifyForPayment when payment date is 5 days away"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.paymentDate >> today.plusDays(5)
        transaction.transactionName >> 'Test Transaction'
        transaction.amount >> 100.0
        transaction.farm >> Mock(Farm) {
            getId() >> 1
            getIsActive() >> true
        }
        User user = Mock(User)
        user.email >> 'test@example.com'
        user.isActive >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(_ as User, _, 'Niedługo upływa termin płatności!')
    }

    def "should checkAndNotifyForPayment when payment date is 1 day away"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> today.plusDays(1)
        transaction.getTransactionName() >> 'Test Transaction'
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
            getIsActive() >> true
        }
        User user = Mock(User)
        user.getEmail() >> 'test@example.com'
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(_ as User, _, 'Niedługo upływa termin płatności!')
    }

    def "should checkAndNotifyForPayment when payment date is not 5 or 1 day away"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> today.plusDays(3)
        transaction.getTransactionName() >> 'Test Transaction'
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
        }
        User user = Mock(User)
        user.getEmail() >> 'test@example.com'
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        0 * mainNotificationService.sendNotificationToUser(_ as User, _, 'Niedługo upływa termin płatności!')
    }

    def "should checkAndNotifyForPayment when transaction has no payment date"() {
        given:
        LocalDate today = LocalDate.now()
        Transaction transaction = Mock(Transaction)
        transaction.getPaymentDate() >> null
        transaction.getTransactionName() >> 'Test Transaction'
        transaction.getAmount() >> 100.0
        transaction.getFarm() >> Mock(Farm) {
            getId() >> 1
        }
        User user = Mock(User)
        user.getEmail() >> 'test@example.com'
        user.getIsActive() >> true
        userService.getAllOwnersForFarm(1) >> [user]

        when:
        financeNotificationService.checkAndNotifyForPayment(transaction, today)

        then:
        0 * mainNotificationService.sendNotificationToUser(_ as User, _, 'Niedługo upływa termin płatności!')
    }

}
