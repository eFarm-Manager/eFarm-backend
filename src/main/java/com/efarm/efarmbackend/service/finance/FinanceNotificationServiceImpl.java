package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository;
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.MainNotificationService;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FinanceNotificationServiceImpl implements FinanceNotificationService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final FinancialCategoryRepository financialCategoryRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final MainNotificationService mainNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(FinanceNotificationServiceImpl.class);

    @Override
    @Transactional
    public void checkPaymentDueDateNotifications() {
        LocalDate today = LocalDate.now();
        FinancialCategory financialCategory = financialCategoryRepository.findByName(EFinancialCategory.EXPENSE);
        PaymentStatus paymentStatus = paymentStatusRepository.findByName(EPaymentStatus.UNPAID);
        List<Transaction> transactions = transactionRepository.findByfinancialCategoryAndPaymentStatus(financialCategory, paymentStatus);
        for (Transaction transaction : transactions) {
            checkAndNotifyForPayment(transaction, today);
        }
    }

    @Override
    public void checkAndNotifyForPayment(Transaction transaction, LocalDate today) {
        if (transaction.getPaymentDate() != null) {
            long daysUntilPayment = ChronoUnit.DAYS.between(today, transaction.getPaymentDate());

            if ((daysUntilPayment == 5 || daysUntilPayment == 1) && transaction.getFarm().getIsActive()) {
                String message = String.format(
                        "Niedługo zbliża się termin płatności: %s (kwota: %.2f), termin upływa za %d dni.",
                        transaction.getTransactionName(), transaction.getAmount(), daysUntilPayment
                );

                List<User> owners = userService.getAllOwnersForFarm(transaction.getFarm().getId());
                for (User owner : owners) {
                    if (owner.getIsActive()) {
                        mainNotificationService.sendNotificationToUser(owner, message, "Niedługo upływa termin płatności!");
                        logger.info("Sending payment due notification to owner: {}", owner.getEmail());
                    }
                }
            }
        }
    }
}
