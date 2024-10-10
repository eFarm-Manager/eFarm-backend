package com.efarm.efarmbackend.repository.finance;

import com.efarm.efarmbackend.model.finance.Transaction;
import com.efarm.efarmbackend.model.finance.TransactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {
    List<Transaction> findByFarmId(Integer farmId);

    Boolean existsByTransactionNameAndFarmId(String transactionName, Integer farmId);

    @Query("SELECT MAX(t.id.id) FROM Transaction t WHERE t.id.farmId = :farmId")
    Optional<Integer> findMaxIdForFarm(@Param("farmId") Integer farmId);

    default Integer findNextFreeIdForFarm(Integer farmId) {
        return findMaxIdForFarm(farmId).map(maxId -> maxId + 1).orElse(1);
    }
}
