package com.efarm.efarmbackend.repository.finance;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.efarm.efarmbackend.model.finance.EPaymentStatus;
import com.efarm.efarmbackend.model.finance.PaymentStatus;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class PaymentStatusRepositoryIT {
    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    @Autowired
    private TestEntityManager entityManager;

	@Test
	public void testFindAwaitingPayment() {
		// when
		PaymentStatus found = paymentStatusRepository.findByName(EPaymentStatus.AWAITING_PAYMENT);

		// then
		assertThat(found.getName(), is(EPaymentStatus.AWAITING_PAYMENT)); 
	}
	
	@Test
	public void testFindPaid() {
		// when
		PaymentStatus found = paymentStatusRepository.findByName(EPaymentStatus.PAID);

		// then
		assertThat(found.getName(), is(EPaymentStatus.PAID)); 
	}

	@Test
	public void testFindUnpaid() {
		// when
		PaymentStatus found = paymentStatusRepository.findByName(EPaymentStatus.UNPAID);

		// then
		assertThat(found.getName(), is(EPaymentStatus.UNPAID)); 
	}
}
