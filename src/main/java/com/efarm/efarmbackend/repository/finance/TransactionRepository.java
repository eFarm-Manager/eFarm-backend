package com.efarm.efarmbackend.repository.finance;

import com.efarm.efarmbackend.model.finance.FinancialCategory;
import com.efarm.efarmbackend.model.finance.PaymentStatus;
import com.efarm.efarmbackend.model.finance.Transaction;
import com.efarm.efarmbackend.model.finance.TransactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {
    List<Transaction> findByFarmId(Integer farmId);

    Boolean existsByTransactionNameAndFarmId(String transactionName, Integer farmId);

    @Query("SELECT t FROM Transaction t WHERE t.farm.id = :farmId " +
            "AND (:searchQuery IS NULL OR LOWER(t.transactionName) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
            "AND (:minDate IS NULL OR t.transactionDate >= :minDate) " +
            "AND (:maxDate IS NULL OR t.transactionDate <= :maxDate) " +
            "AND (:financialCategory IS NULL OR t.financialCategory = :financialCategory) " +
            "AND (:paymentStatus IS NULL OR t.paymentStatus = :paymentStatus) " +
            "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
            "ORDER BY " +
            "t.transactionDate DESC")
    List<Transaction> findFilteredTransactions(@Param("farmId") Integer farmId,
                                               @Param("searchQuery") String searchQuery,
                                               @Param("minDate") LocalDate minDate,
                                               @Param("maxDate") LocalDate maxDate,
                                               @Param("financialCategory") FinancialCategory financialCategory,
                                               @Param("paymentStatus") PaymentStatus paymentStatus,
                                               @Param("minAmount") Double minAmount,
                                               @Param("maxAmount") Double maxAmount);

    List<Transaction> findByfinancialCategoryAndPaymentStatus(FinancialCategory financialCategory, PaymentStatus paymentStatus);

    @Query("SELECT t FROM Transaction t WHERE t.farm.id = :farmId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByFarmAndDate(@Param("farmId") Integer farmId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT MAX(t.id.id) FROM Transaction t WHERE t.id.farmId = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }
}
