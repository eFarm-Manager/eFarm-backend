package com.efarm.efarmbackend.repository.finance;

import com.efarm.efarmbackend.model.finance.EPaymentStatus;
import com.efarm.efarmbackend.model.finance.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {
    PaymentStatus findByName(EPaymentStatus paymentStatusEnum);
}
