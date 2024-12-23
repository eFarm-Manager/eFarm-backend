package com.efarm.efarmbackend.service.finance

import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.finance.*
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest
import com.efarm.efarmbackend.payload.response.BalanceResponse
import com.efarm.efarmbackend.repository.finance.FinancialCategoryRepository
import com.efarm.efarmbackend.repository.finance.PaymentStatusRepository
import com.efarm.efarmbackend.repository.finance.TransactionRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class FinanceServiceSpec extends Specification {

    def financialCategoryRepository = Mock(FinancialCategoryRepository)
    def paymentStatusRepository = Mock(PaymentStatusRepository)
    def transactionRepository = Mock(TransactionRepository)

    @Subject
    FinanceServiceimpl financeService = new FinanceServiceimpl(
            financialCategoryRepository,
            paymentStatusRepository,
            transactionRepository
    )

    /*
    * addNewTransactionData
    */

    def "should add new transaction"() {
        given:
        NewTransactionRequest newTransactionRequest = new NewTransactionRequest(
                financialCategory: "INCOME",
                paymentStatus: "PAID",
                transactionName: "Transaction",
                transactionDate: LocalDate.now().plusDays(1),
                paymentDate: LocalDate.now().plusDays(6),
                amount: 1000.0,
                description: "Description"
        )
        TransactionId transactionId = new TransactionId()
        Farm farm = Mock(Farm)

        when:
        Transaction result = financeService.addNewTransactionData(transactionId, farm, newTransactionRequest)

        then:
        result.id == transactionId
        result.farm == farm
        result.amount == newTransactionRequest.amount
    }

    /*
    * updateTransactionProperties
    */

    def "should update transaction"() {
        given:
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest(
                financialCategory: "INCOME",
                paymentStatus: "PAID",
                transactionName: "Transaction",
                transactionDate: LocalDate.now().plusDays(1),
                paymentDate: LocalDate.now().plusDays(6),
                amount: 1000.0,
                description: "Description"
        )
        FinancialCategory financialCategory = Mock(FinancialCategory) {
            getName() >> EFinancialCategory.valueOf(updateTransactionRequest.getFinancialCategory())
        }
        PaymentStatus paymentStatus = Mock(PaymentStatus) {
            getName() >> EPaymentStatus.valueOf(updateTransactionRequest.getPaymentStatus())
        }
        Transaction transaction = Mock(Transaction) {
            getFinancialCategory() >> financialCategory
            getPaymentStatus() >> paymentStatus
        }

        financialCategoryRepository.findByName(EFinancialCategory.valueOf(updateTransactionRequest.getFinancialCategory())) >> financialCategory
        paymentStatusRepository.findByName(EPaymentStatus.valueOf(updateTransactionRequest.getPaymentStatus())) >> paymentStatus

        when:
        financeService.updateTransactionProperties(transaction, updateTransactionRequest)

        then:
        1 * transaction.setAmount(updateTransactionRequest.amount)
        1 * transaction.setTransactionName(updateTransactionRequest.transactionName)
        1 * transaction.setTransactionDate(updateTransactionRequest.transactionDate)
        1 * transaction.setPaymentDate(updateTransactionRequest.paymentDate)
        1 * transaction.setDescription(updateTransactionRequest.description)
    }

    /*
    * checkTransactionAlreadyExistsByName
    */

    def "should check that transaction doesnt already exists by name"() {
        given:
        String transactionName = "Transaction"
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        transactionRepository.existsByTransactionNameAndFarmId(transactionName, farm.getId()) >> false

        when:
        financeService.checkTransactionAlreadyExistsByName(farm, transactionName)

        then:
        noExceptionThrown()
    }

    def " should check that transaction already exists by name and throw exception"() {
        given:
        String transactionName = "Transaction"
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        transactionRepository.existsByTransactionNameAndFarmId(transactionName, farm.getId()) >> true

        when:
        financeService.checkTransactionAlreadyExistsByName(farm, transactionName)

        then:
        thrown(Exception)
    }
    /*
    * getFinancialCategoryForFiltering
    */

    def "should get financial category for filter"() {
        given:
        String financialCategoryName = "INCOME"
        FinancialCategory financialCategory = Mock(FinancialCategory)
        financialCategoryRepository.findByName(EFinancialCategory.INCOME) >> financialCategory

        when:
        FinancialCategory result = financeService.getFinancialCategoryForFiltering(financialCategoryName)

        then:
        result.getName() == financialCategory.getName()
    }

    /*
    * getPaymentStatusForFiltering
    */

    def "should get payment status for filter"() {
        given:
        String paymentStatusName = "PAID"
        PaymentStatus paymentStatus = Mock(PaymentStatus)
        paymentStatusRepository.findByName(EPaymentStatus.PAID) >> paymentStatus

        when:
        PaymentStatus result = financeService.getPaymentStatusForFiltering(paymentStatusName)

        then:
        result.getName() == paymentStatus.getName()
    }

    /*
    * getTransactionsByFarmAndDate
    */

    def "should get transactions by farm and date"() {
        given:
        Integer farmId = 1
        LocalDate startDate = LocalDate.now().minusDays(1)
        LocalDate endDate = LocalDate.now().plusDays(1)
        List<Transaction> expectedTransactions = [
                Mock(Transaction) {
                    getId() >> new TransactionId(1, farmId)
                    getTransactionName() >> "Transaction1"
                    getTransactionDate() >> LocalDate.of(2023, 5, 15)
                },
                Mock(Transaction) {
                    getId() >> new TransactionId(2, farmId)
                    getTransactionName() >> "Transaction2"
                    getTransactionDate() >> LocalDate.of(2023, 6, 20)
                }
        ]
        transactionRepository.findByFarmAndDate(farmId, startDate, endDate) >> expectedTransactions

        when:
        List<Transaction> result = financeService.getTransactionsByFarmAndDate(farmId, startDate, endDate)

        then:
        result != null
        result.size() == expectedTransactions.size()
    }

    /*
    * deleteAllTransactionsForFarm
    */

    def "should delete all transactions for given farm"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Transaction transaction1 = Mock(Transaction) {
            getId() >> new TransactionId(1, farm.getId())
        }

        Transaction transaction2 = Mock(Transaction) {
            getId() >> new TransactionId(2, farm.getId())
        }

        transactionRepository.findByFarmId(farm.getId()) >> [transaction1, transaction2]

        when:
        financeService.deleteAllTransactionsForFarm(farm)

        then:
        1 * transactionRepository.deleteAll({ List<Transaction> transactions ->
            transactions.contains(transaction1) && transactions.contains(transaction2)
        })

    }

    /*
    * calculateFarmBalance
    */

    def "should calculate farm balance correctly"() {
        given:
        List<Transaction> transactions = [
                new Transaction(amount: 100.0, financialCategory: new FinancialCategory(name: EFinancialCategory.INCOME), paymentStatus: new PaymentStatus(name: EPaymentStatus.PAID)),
                new Transaction(amount: 50.0, financialCategory: new FinancialCategory(name: EFinancialCategory.EXPENSE), paymentStatus: new PaymentStatus(name: EPaymentStatus.PAID)),
                new Transaction(amount: 200.0, financialCategory: new FinancialCategory(name: EFinancialCategory.INCOME), paymentStatus: new PaymentStatus(name: EPaymentStatus.UNPAID)),
                new Transaction(amount: 75.0, financialCategory: new FinancialCategory(name: EFinancialCategory.EXPENSE), paymentStatus: new PaymentStatus(name: EPaymentStatus.UNPAID))
        ]

        when:
        BalanceResponse balanceResponse = financeService.calculateFarmBalance(transactions)

        then:
        balanceResponse.totalIncome == 300.0
        balanceResponse.totalExpense == 125.0
        balanceResponse.balance == 175.0
        balanceResponse.toPay == 75.0
        balanceResponse.toReceive == 200.0
    }

    /*
    * setNewTransactionPaymentStatus
    */

    def "should set new transaction payment status correctly"() {
        given:
        Transaction transaction = new Transaction()
        String paymentStatusName = "PAID"
        EPaymentStatus paymentStatusEnum = EPaymentStatus.PAID
        PaymentStatus paymentStatus = new PaymentStatus(name: paymentStatusEnum)
        paymentStatusRepository.findByName(paymentStatusEnum) >> paymentStatus

        when:
        financeService.setNewTransactionPaymentStatus(transaction, paymentStatusName)

        then:
        transaction.getPaymentStatus().getName() == paymentStatus.getName()
    }

    /*
    * setNewTransactionFinancialCategory
    */

    def "should set new transaction financial category correctly"() {
        given:
        Transaction transaction = new Transaction()
        String financialCategoryName = "INCOME"
        EFinancialCategory financialCategoryEnum = EFinancialCategory.INCOME
        FinancialCategory financialCategory = new FinancialCategory(name: financialCategoryEnum)
        financialCategoryRepository.findByName(financialCategoryEnum) >> financialCategory

        when:
        financeService.setNewTransactionFinancialCategory(transaction, financialCategoryName)

        then:
        transaction.getFinancialCategory().getName() == financialCategory.getName()
    }


}
