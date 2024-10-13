package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.model.user.User;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FinanceNotificationService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Autowired
    private FinancialCategoryRepository financialCategoryRepository;

    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    private static final Logger logger = LoggerFactory.getLogger(FinanceNotificationService.class);

    public void checkPaymentDueDateNotifications() {
        LocalDate today = LocalDate.now();
        FinancialCategory financialCategory = financialCategoryRepository.findByName(EFinancialCategory.EXPENSE);
        PaymentStatus paymentStatus = paymentStatusRepository.findByName(EPaymentStatus.UNPAID);
        List<Transaction> transactions = transactionRepository.findByfinancialCategoryAndPaymentStatus(financialCategory, paymentStatus);
        for (Transaction transaction : transactions) {
            checkAndNotifyForPayment(transaction, today);
        }
    }

    private void sendNotificationToOwner(User owner, String message, String subject) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(owner.getEmail());
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }

    private void checkAndNotifyForPayment(Transaction transaction, LocalDate today) {
        if (transaction.getPaymentDate() != null) {
            long daysUntilPayment = ChronoUnit.DAYS.between(today, transaction.getPaymentDate());

            if (daysUntilPayment == 5 || daysUntilPayment == 1) {
                String message = String.format(
                        "Niedługo zbliża się termin płatności: %s (kwota: %.2f), termin upływa za %d dni.",
                        transaction.getTransactionName(), transaction.getAmount(), daysUntilPayment
                );
                List<User> owners = userService.getAllOwnersForFarm(transaction.getFarm().getId());
                for (User owner : owners) {
                    if(owner.getIsActive()) {
                        sendNotificationToOwner(owner, message, "Niedługo upływa termin płatności!");
                        logger.info("Sending payment due notification to owner: {}", owner.getEmail());
                    }
                }
            }
        }
    }
}
