package com.efarm.efarmbackend.service.finance

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.*;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import spock.lang.Specification
import spock.lang.Subject

class FinanceFacadeSpec extends Specification {

    def financeService = Mock(FinanceService)
    def transactionRepository = Mock(TransactionRepository)
    def userService = Mock(UserService)
	
    @Subject
    FinanceFacade financeFacade = new FinanceFacade(
        financeService: financeService,
        transactionRepository: transactionRepository,
        userService: userService
    )
	/*
	* The test below is a unit test for the addNewTransaction method in the FinanceFacade class.
	*/

    def "should add new transaction"() {
        given:
        NewTransactionRequest newTransactionRequest = new NewTransactionRequest(transactionName: "Test Transaction")
        Farm loggedUserFarm = new Farm(id: 1)
        TransactionId transactionId = new TransactionId(1, loggedUserFarm.getId())
        Transaction transaction = new Transaction(id: transactionId)

        userService.getLoggedUserFarm() >> loggedUserFarm
        transactionRepository.findNextFreeIdForFarm(loggedUserFarm.getId()) >> 1
        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, newTransactionRequest.getTransactionName()) >>{ } 
        financeService.addNewTransactionData(transactionId, loggedUserFarm, newTransactionRequest) >> transaction

        when:
        financeFacade.addNewTransaction(newTransactionRequest)

        then:
        1 * transactionRepository.save(_)
    }

    def "should not add new transaction because it already exists by name"() {
        given:
        NewTransactionRequest newTransactionRequest = new NewTransactionRequest(transactionName: "Test Transaction")
        Farm loggedUserFarm = new Farm(id: 1)

        userService.getLoggedUserFarm() >> loggedUserFarm
        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, newTransactionRequest.getTransactionName()) >> { throw new Exception() }	

        when:
        financeFacade.addNewTransaction(newTransactionRequest)

        then:
        thrown(Exception)
        0 * transactionRepository.save(_)
    }

	/*
	* The test below is a unit test for the updateTransaction method in the FinanceFacade class. 
	*/
    def "should update transaction"() {
        given:
        Integer id = 1
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest()
        Farm loggedUserFarm = new Farm(id: 1)
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId())
        Transaction transaction = new Transaction(id: transactionId)
        userService.getLoggedUserFarm() >> loggedUserFarm
        transactionRepository.findById(transactionId) >> Optional.of(transaction)
        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, updateTransactionRequest.getTransactionName()) >> { }
        financeService.updateTransactionProperties(transaction, updateTransactionRequest) >> { }

        when:
        financeFacade.updateTransaction(id, updateTransactionRequest)

        then:
        1 * transactionRepository.save(_)
    }

    def "should fail to update transaction when transaction does not exist"() {
        given:
        Integer id = 1
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest()
        Farm loggedUserFarm = new Farm(id: 1)
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId())
        userService.getLoggedUserFarm() >> loggedUserFarm
        transactionRepository.findById(transactionId) >> Optional.empty()

        when:
        financeFacade.updateTransaction(id, updateTransactionRequest)

        then:
        thrown(RuntimeException)
        0 * transactionRepository.save(_)
    }

    def "should fail to update transaction when transaction doesnt already exists by name"() {
        given:
        Integer id = 1
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest(transactionName: "Existing Transaction")
        Farm loggedUserFarm = new Farm(id: 1)
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId())
        Transaction transaction = new Transaction(id: transactionId)
        userService.getLoggedUserFarm() >> loggedUserFarm
        transactionRepository.findById(transactionId) >> Optional.of(transaction)
        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, updateTransactionRequest.getTransactionName()) >> { throw new Exception() }

        when:
        financeFacade.updateTransaction(id, updateTransactionRequest)

        then:
        thrown(Exception)
        0 * transactionRepository.save(_)
    }
	/*
	* The test below is a unit test for the deleteTransaction method in the FinanceFacade class.
	*/

    def "should delete transaction"() {
        given:
        Integer id = 1
        Farm loggedUserFarm = new Farm(id: 1)
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId())
        Transaction transaction = new Transaction(id: transactionId)
        userService.getLoggedUserFarm() >> loggedUserFarm
        transactionRepository.findById(transactionId) >> Optional.of(transaction)

        when:
        financeFacade.deleteTransaction(id)

        then:
        1 * transactionRepository.delete(_)
    }

    def "should fail to delete transaction when transaction does not exist"() {
        given:
        Integer id = 1
        Farm loggedUserFarm = new Farm(id: 1)
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId())
        userService.getLoggedUserFarm() >> loggedUserFarm
        transactionRepository.findById(transactionId) >> Optional.empty()

        when:
        financeFacade.deleteTransaction(id)

        then:
        thrown(RuntimeException)
        0 * transactionRepository.delete(_)
    }
	/*
	* The test below is a unit test for the getTransactions method in the FinanceFacade class.
	*/

    def "should get transactions successfully"() {
        given:
        String searchQuery = "test"
        LocalDate minDate = LocalDate.of(2024, 1, 1)
        LocalDate maxDate = LocalDate.of(2025, 12, 31)
        String financialCategoryString = "EXPENSE"
        String paymentStatusString = "PAID"
        Double minAmount = 100.0
        Double maxAmount = 1000.0
        Farm loggedUserFarm = new Farm(id: 1)
        FinancialCategory financialCategory = new FinancialCategory(name: EFinancialCategory.EXPENSE)
        PaymentStatus paymentStatus = new PaymentStatus(name: EPaymentStatus.PAID)
        Transaction transaction = new Transaction(id: new TransactionId(1, loggedUserFarm.getId()), transactionName: "Test Transaction", amount: 500.0, financialCategory: financialCategory, paymentStatus: paymentStatus, transactionDate: LocalDate.now())
        List<Transaction> transactions = [transaction]

        userService.getLoggedUserFarm() >> loggedUserFarm
        financeService.getFinancialCategoryForFiltering(financialCategoryString) >> financialCategory
        financeService.getPaymentStatusForFiltering(paymentStatusString) >> paymentStatus
        transactionRepository.findFilteredTransactions(loggedUserFarm.getId(), searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount) >> transactions

        when:
        List<TransactionDTO> result = financeFacade.getTransactions(searchQuery, minDate, maxDate, financialCategoryString, paymentStatusString, minAmount, maxAmount)

        then:
        result.size() == 1
        result[0].id == transaction.id.id
	result[0].amount == transaction.amount
	result[0].transactionName == transaction.transactionName
	result[0].financialCategory == transaction.financialCategory.name.toString()
	result[0].paymentStatus == transaction.paymentStatus.name.toString()
	result[0].transactionDate == transaction.transactionDate
    }

	def "should return empty list when no transactions match criteria"() {
    given:
    String searchQuery = "nonexistent"
    LocalDate minDate = LocalDate.of(2024, 1, 1)
    LocalDate maxDate = LocalDate.of(2025, 12, 31)
    String financialCategoryString = "EXPENSE"
    String paymentStatusString = "PAID"
    Double minAmount = 100.0
    Double maxAmount = 1000.0
    Farm loggedUserFarm = new Farm(id: 1)
    FinancialCategory financialCategory = new FinancialCategory(name: EFinancialCategory.EXPENSE)
    PaymentStatus paymentStatus = new PaymentStatus(name: EPaymentStatus.PAID)

    userService.getLoggedUserFarm() >> loggedUserFarm
    financeService.getFinancialCategoryForFiltering(financialCategoryString) >> financialCategory
    financeService.getPaymentStatusForFiltering(paymentStatusString) >> paymentStatus
    transactionRepository.findFilteredTransactions(loggedUserFarm.getId(), searchQuery, minDate, maxDate, financialCategory, paymentStatus, minAmount, maxAmount) >> []

    when:
    List<TransactionDTO> result = financeFacade.getTransactions(searchQuery, minDate, maxDate, financialCategoryString, paymentStatusString, minAmount, maxAmount)

    then:
    result.isEmpty()
}

	def "should handle null filters and return all transactions"() {
    given:
    String searchQuery = null
    LocalDate minDate = null
    LocalDate maxDate = null
    String financialCategoryString = null
    String paymentStatusString = null
    Double minAmount = null
    Double maxAmount = null
    Farm loggedUserFarm = new Farm(id: 1)
    Transaction transaction1 = new Transaction(id: new TransactionId(1, loggedUserFarm.getId()), transactionName: "Transaction 1", amount: 200.0, transactionDate: LocalDate.of(2024, 6, 1), financialCategory: new FinancialCategory(name: EFinancialCategory.EXPENSE), paymentStatus: new PaymentStatus(name: EPaymentStatus.PAID))
    Transaction transaction2 = new Transaction(id: new TransactionId(2, loggedUserFarm.getId()), transactionName: "Transaction 2", amount: 300.0, transactionDate: LocalDate.of(2025, 7, 15), financialCategory: new FinancialCategory(name: EFinancialCategory.INCOME), paymentStatus: new PaymentStatus(name: EPaymentStatus.UNPAID))
    List<Transaction> transactions = [transaction1, transaction2]

    userService.getLoggedUserFarm() >> loggedUserFarm
    financeService.getFinancialCategoryForFiltering(financialCategoryString) >> null
    financeService.getPaymentStatusForFiltering(paymentStatusString) >> null
    transactionRepository.findFilteredTransactions(loggedUserFarm.getId(), searchQuery, minDate, maxDate, null, null, minAmount, maxAmount) >> transactions

    when:
    List<TransactionDTO> result = financeFacade.getTransactions(searchQuery, minDate, maxDate, financialCategoryString, paymentStatusString, minAmount, maxAmount)

    then:
    result.size() == 2
    result*.transactionName.containsAll(["Transaction 1", "Transaction 2"])
}

	/*
	* The test below is a unit test for the getBalanceForLastYear method in the FinanceFacade class.
	*/

    def "should get balance for last year successfully"() {
        given:
        Farm loggedUserFarm = new Farm(id: 1)
        Integer farmId = loggedUserFarm.getId()
        LocalDate oneYearAgo = LocalDate.now().minusYears(1)
        List<Transaction> transactions = [new Transaction(id: new TransactionId(1, farmId))]
        BalanceResponse balanceResponse = new BalanceResponse()

        userService.getLoggedUserFarm() >> loggedUserFarm
        financeService.getTransactionsByFarmAndDate(farmId, oneYearAgo, LocalDate.now()) >> transactions
        financeService.calculateFarmBalance(transactions) >> balanceResponse

        when:
        BalanceResponse result = financeFacade.getBalanceForLastYear()

        then:
        result == balanceResponse
    }

	/*
	* The test below is a unit test for the mapToDTO method in the FinanceFacade class.
	*/
	
    def "should map transaction to DTO successfully"() {
        given:
        TransactionId transactionId = new TransactionId(1, 1)
        FinancialCategory financialCategory = new FinancialCategory(name: EFinancialCategory.EXPENSE)
        PaymentStatus paymentStatus = new PaymentStatus(name: EPaymentStatus.PAID)
        Transaction transaction = new Transaction(
            id: transactionId,
            transactionName: "Test Transaction",
            amount: 100.0,
            financialCategory: financialCategory,
            paymentStatus: paymentStatus,
            transactionDate: LocalDate.of(2023, 1, 1),
            paymentDate: LocalDate.of(2023, 1, 2),
            description: "Test Description"
        )

        when:
        TransactionDTO result = financeFacade.mapToDTO(transaction)

        then:
        result.id == transactionId.getId()
        result.transactionName == "Test Transaction"
        result.amount == 100.0
        result.financialCategory == "EXPENSE"
        result.paymentStatus == "PAID"
        result.transactionDate == LocalDate.of(2023, 1, 1)
        result.paymentDate == LocalDate.of(2023, 1, 2)
        result.description == "Test Description"
    }
}
