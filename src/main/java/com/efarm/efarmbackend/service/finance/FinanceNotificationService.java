package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.finance.Transaction;
import jakarta.transaction.Transactional;

import java.time.LocalDate;

public interface FinanceNotificationService {
    @Transactional
    void checkPaymentDueDateNotifications();

    void checkAndNotifyForPayment(Transaction transaction, LocalDate today);
}
