package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.Transaction;
import com.efarm.efarmbackend.model.finance.TransactionId;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinanceFacade {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(FinanceFacade.class);


    @Transactional
    public void addNewTransaction(NewTransactionRequest newTransactionRequest) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(transactionRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());

        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, newTransactionRequest.getTransactionName());
        Transaction transaction = financeService.addNewTransactionData(transactionId, loggedUserFarm, newTransactionRequest);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransaction(Integer id, UpdateTransactionRequest updateTransactionRequest) throws Exception {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transakcja nie została znaleziona"));

        financeService.checkTransactionAlreadyExistsByName(loggedUserFarm, updateTransactionRequest.getTransactionName());
        financeService.updateTransactionProperties(transaction, updateTransactionRequest);
        transactionRepository.save(transaction);
    }

    public void deleteTransaction(Integer id) {
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(id, loggedUserFarm.getId());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transakcja nie została znaleziona"));
        transactionRepository.delete(transaction);
    }
}
