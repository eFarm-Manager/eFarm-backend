package com.efarm.efarmbackend.service.finance;

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.Transaction;
import com.efarm.efarmbackend.model.finance.TransactionId;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.repository.finance.TransactionRepository;
import com.efarm.efarmbackend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class FinanceFacade {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;


    @Transactional
    public void addNewTransaction(NewTransactionRequest newTransactionRequest) throws Exception{
        Farm loggedUserFarm = userService.getLoggedUserFarm();
        TransactionId transactionId = new TransactionId(transactionRepository.findNextFreeIdForFarm(loggedUserFarm.getId()), loggedUserFarm.getId());

        if (transactionRepository.existsByTransactionNameAndFarmId(newTransactionRequest.getTransactionName(), loggedUserFarm.getId())) {
            throw new Exception("Transakcja o podanej nazwie już istnieje!");
        }

        Transaction transaction = financeService.addNewTransactionData(transactionId, loggedUserFarm, newTransactionRequest);
        transactionRepository.save(transaction);
    }
}
