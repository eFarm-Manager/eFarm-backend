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
import com.efarm.efarmbackend.model.finance.EFinancialCategory;
import com.efarm.efarmbackend.model.finance.FinancialCategory;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FinancialCategoryRepositoryIT {
    @Autowired
    private FinancialCategoryRepository financialCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;
	
	@Test
	public void testFindIncomeByName() {
		// when
		FinancialCategory found = financialCategoryRepository.findByName(EFinancialCategory.INCOME);
		
		// then
		assertThat(found.getName(), is(EFinancialCategory.INCOME));
	}
	
	@Test
	public void testFindExpenseByName() {
		// when
		FinancialCategory found = financialCategoryRepository.findByName(EFinancialCategory.EXPENSE);
		
		// then
		assertThat(found.getName(), is(EFinancialCategory.EXPENSE));
	}
}
