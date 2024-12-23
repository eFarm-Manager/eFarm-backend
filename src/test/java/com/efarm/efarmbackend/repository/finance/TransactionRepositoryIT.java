package com.efarm.efarmbackend.repository.finance;

import com.efarm.efarmbackend.model.farm.ActivationCode;
import com.efarm.efarmbackend.model.farm.Address;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class TransactionRepositoryIT {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindAllTransactionsByFarmId() throws Exception {
        // given
        Farm farm = entityManager.find(Farm.class, 1);
        Long transactionCount = entityManager.getEntityManager().createQuery("SELECT COUNT(t) FROM Transaction t WHERE t.farm = :farm", Long.class)
                .setParameter("farm", farm)
                .getSingleResult();
        // when
        List<Transaction> transactions = transactionRepository.findByFarmId(1);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactionCount.intValue(), is(transactions.size()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(1)))));
    }

    @Test
    public void testExistsByTransactionNameAndFarmId() throws Exception {
        // given
        Farm farm = entityManager.find(Farm.class, 1);
        Transaction transaction = entityManager.find(Transaction.class, new TransactionId(1, 1));
        String transactionName = transaction.getTransactionName();
        // when
        boolean exists = transactionRepository.existsByTransactionNameAndFarmId(transactionName, 1);
        // then
        assertThat(exists, is(true));
    }

    @Test
    public void testDoesntExistsByTransactionNameAndFarmId() throws Exception {
        // given
        String transactionName = "nonexistent";
        // when
        boolean exists = transactionRepository.existsByTransactionNameAndFarmId(transactionName, 1);
        // then
        assertThat(exists, is(false));
    }

    @Test
    public void testFindFilteredTransactions() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = LocalDate.of(2023, 1, 1);
        LocalDate maxDate = LocalDate.of(2028, 12, 31);
        FinancialCategory financialCategory = entityManager.getEntityManager().createQuery(
                        "SELECT fc FROM FinancialCategory fc WHERE fc.name = :name", FinancialCategory.class)
                .setParameter("name", EFinancialCategory.EXPENSE)
                .getSingleResult();
        PaymentStatus paymentStatus = entityManager.getEntityManager().createQuery(
                        "SELECT ps FROM PaymentStatus ps WHERE ps.name = :name", PaymentStatus.class)
                .setParameter("name", EPaymentStatus.AWAITING_PAYMENT)
                .getSingleResult();
        Double minAmount = 10.0;
        Double maxAmount = 10000.0;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("transactionDate", allOf(greaterThanOrEqualTo(minDate), lessThanOrEqualTo(maxDate)))));
        assertThat(transactions, everyItem(hasProperty("financialCategory", hasProperty("name", is(financialCategory.getName())))));
        assertThat(transactions, everyItem(hasProperty("paymentStatus", hasProperty("name", is(paymentStatus.getName())))));
        assertThat(transactions, everyItem(hasProperty("amount", allOf(greaterThanOrEqualTo(minAmount), lessThanOrEqualTo(maxAmount)))));
    }

    @Test
    public void testFindFilteredTransactionsByMinDate() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = LocalDate.of(2023, 1, 1);
        LocalDate maxDate = null;
        FinancialCategory financialCategory = null;
        PaymentStatus paymentStatus = null;
        Double minAmount = null;
        Double maxAmount = null;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("transactionDate", greaterThanOrEqualTo(minDate))));
    }

    @Test
    public void testFindFilteredTransactionsBySearchQuery() {
        // given
        Integer farmId = 1;
        Transaction transaction = entityManager.find(Transaction.class, new TransactionId(1, 1));
        String searchQuery = transaction.getTransactionName().substring(0, 5);
        LocalDate minDate = null;
        LocalDate maxDate = null;
        FinancialCategory financialCategory = null;
        PaymentStatus paymentStatus = null;
        Double minAmount = null;
        Double maxAmount = null;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("transactionName", containsStringIgnoringCase(searchQuery))));
    }

    @Test
    public void testFindFilteredTransactionsByMaxDate() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = null;
        LocalDate maxDate = LocalDate.of(2028, 12, 31);
        FinancialCategory financialCategory = null;
        PaymentStatus paymentStatus = null;
        Double minAmount = null;
        Double maxAmount = null;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("transactionDate", lessThanOrEqualTo(maxDate))));
    }

    @Test
    public void testFindFilteredTransactionsByFinancialCategory() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = null;
        LocalDate maxDate = null;
        FinancialCategory financialCategory = entityManager.getEntityManager().createQuery(
                        "SELECT fc FROM FinancialCategory fc WHERE fc.name = :name", FinancialCategory.class)
                .setParameter("name", EFinancialCategory.EXPENSE)
                .getSingleResult();
        PaymentStatus paymentStatus = null;
        Double minAmount = null;
        Double maxAmount = null;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("financialCategory", hasProperty("name", is(financialCategory.getName())))));
    }

    @Test
    public void testFindFilteredTransactionsByPaymentStatus() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = null;
        LocalDate maxDate = null;
        FinancialCategory financialCategory = null;
        PaymentStatus paymentStatus = entityManager.getEntityManager().createQuery(
                        "SELECT ps FROM PaymentStatus ps WHERE ps.name = :name", PaymentStatus.class)
                .setParameter("name", EPaymentStatus.PAID)
                .getSingleResult();
        Double minAmount = null;
        Double maxAmount = null;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("paymentStatus", hasProperty("name", is(paymentStatus.getName())))));
    }

    @Test
    public void testFindFilteredTransactionsByMinAmount() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = null;
        LocalDate maxDate = null;
        FinancialCategory financialCategory = null;
        PaymentStatus paymentStatus = null;
        Double minAmount = 10.0;
        Double maxAmount = null;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("amount", greaterThanOrEqualTo(minAmount))));
    }

    @Test
    public void testFindFilteredTransactionsByMaxAmount() {
        // given
        Integer farmId = 1;
        String searchQuery = null;
        LocalDate minDate = null;
        LocalDate maxDate = null;
        FinancialCategory financialCategory = null;
        PaymentStatus paymentStatus = null;
        Double minAmount = null;
        Double maxAmount = 10000.0;
        // when
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                farmId, searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount);
        // then
        assertThat(transactions, not(empty()));
        assertThat(transactions, everyItem(hasProperty("farm", hasProperty("id", is(farmId)))));
        assertThat(transactions, everyItem(hasProperty("amount", lessThanOrEqualTo(maxAmount))));
    }

    @Test
    public void testFindByFinancialCategoryAndPaymentStatus() {
        //given
        FinancialCategory financialCategory = entityManager.getEntityManager().createQuery(
                        "SELECT fc FROM FinancialCategory fc WHERE fc.name = :name", FinancialCategory.class)
                .setParameter("name", EFinancialCategory.EXPENSE)
                .getSingleResult();
        PaymentStatus paymentStatus = entityManager.getEntityManager().createQuery(
                        "SELECT ps FROM PaymentStatus ps WHERE ps.name = :name", PaymentStatus.class)
                .setParameter("name", EPaymentStatus.PAID)
                .getSingleResult();
        //when
        List<Transaction> transactions = transactionRepository.findByfinancialCategoryAndPaymentStatus(financialCategory, paymentStatus);
        //then
        assertThat(transactions, not(empty()));

        assertThat(transactions, everyItem(hasProperty("financialCategory", hasProperty("name", is(financialCategory.getName())))));
        assertThat(transactions, everyItem(hasProperty("paymentStatus", hasProperty("name", is(paymentStatus.getName())))));
    }

    @Test
    public void testFindByFarmAndDate() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        //when
        List<Transaction> transactions = transactionRepository.findByFarmAndDate(farm.getId(), startDate, endDate);
        //then
        assertThat(transactions, not(empty()));

        assertThat(transactions, everyItem(hasProperty("transactionDate", allOf(greaterThanOrEqualTo(startDate), lessThanOrEqualTo(endDate)))));
    }

    @Test
    public void testFindByFarmAndDateNoResults() {
        // given
        Farm farm = entityManager.find(Farm.class, 1);
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2020, 12, 31);
        // when
        List<Transaction> transactions = transactionRepository.findByFarmAndDate(farm.getId(), startDate, endDate);
        // then
        assertThat(transactions, empty());
    }

    @Test
    public void testFindMaxIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);
        Transaction maxTransaction = entityManager.getEntityManager().createQuery(
                        "SELECT t FROM Transaction t WHERE t.farm = :farm ORDER BY t.id DESC", Transaction.class)
                .setParameter("farm", farm)
                .setMaxResults(1)
                .getSingleResult();
        //when
        Optional<Integer> maxId = transactionRepository.findMaxIdForFarm(farm.getId());
        //then
        assertThat(maxId.isPresent(), is(true));
        assertThat(maxId.get(), is(maxTransaction.getId().getId()));
    }


    @Test
    public void testDoesntFindMaxIdForFarm() {
        //given
        Address address = new Address();
        entityManager.persist(address);
        entityManager.flush();
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        Farm farm = new Farm();
        farm.setFarmName("uniqueFarmName");
        farm.setIsActive(true);
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCode.getId());
        entityManager.persist(farm);
        entityManager.flush();
        //when
        Optional<Integer> maxIdFound = transactionRepository.findMaxIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound, is(Optional.empty()));
    }


    @Test
    public void testFindNextMaxFreeTransactionIdForFarm() {
        //given
        Farm farm = entityManager.find(Farm.class, 1);

        Integer maxId = entityManager.getEntityManager()
                .createQuery("SELECT MAX(t.id.id) FROM Transaction t WHERE t.id.farmId = 1", Integer.class)
                .getSingleResult();

        //when
        Integer maxIdFound = transactionRepository.findNextFreeIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound, is(maxId + 1));
    }

    @Test
    public void testFindNextMaxFreeTransactionIdForNewFarm() {
        //given
        Address address = new Address();
        entityManager.persist(address);
        entityManager.flush();
        ActivationCode activationCode = entityManager.find(ActivationCode.class, 1);
        Farm farm = new Farm();
        farm.setFarmName("uniqueFarmName");
        farm.setIsActive(true);
        farm.setIdAddress(address.getId());
        farm.setIdActivationCode(activationCode.getId());
        entityManager.persist(farm);
        entityManager.flush();

        //when
        Integer maxIdFound = transactionRepository.findNextFreeIdForFarm(farm.getId());

        //then
        assertThat(maxIdFound, is(1));
    }

}

