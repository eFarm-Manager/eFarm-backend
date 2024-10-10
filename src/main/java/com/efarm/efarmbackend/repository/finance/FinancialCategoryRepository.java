package com.efarm.efarmbackend.repository.finance;

import com.efarm.efarmbackend.model.finance.EFinancialCategory;
import com.efarm.efarmbackend.model.finance.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, Integer> {
    FinancialCategory findByName(EFinancialCategory financialCategoryEnum);
}
