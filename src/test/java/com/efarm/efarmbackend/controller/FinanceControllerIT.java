package com.efarm.efarmbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentShortDTO;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.finance.Transaction;
import com.efarm.efarmbackend.model.finance.TransactionDTO;
import com.efarm.efarmbackend.model.finance.TransactionId;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.landparcel.LandparcelDTO;
import com.efarm.efarmbackend.model.landparcel.LandparcelId;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.finance.NewTransactionRequest;
import com.efarm.efarmbackend.payload.request.finance.UpdateTransactionRequest;
import com.efarm.efarmbackend.payload.request.landparcel.AddLandparcelRequest;
import com.efarm.efarmbackend.payload.request.landparcel.UpdateLandparcelRequest;
import com.efarm.efarmbackend.payload.response.BalanceResponse;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.security.services.UserDetailsImpl;
import com.efarm.efarmbackend.service.landparcel.LandparcelFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integrationtest")
public class FinanceControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void useOwnerOfFirstFarm() {
        User currentUser = entityManager.find(User.class, 1);
        UserDetailsImpl userDetails = UserDetailsImpl.build(currentUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    /*
    * POST /new
    */
    @Test 
    public void testAddingNewTransaction() throws Exception {
        // Given
        NewTransactionRequest newTransactionRequest = new NewTransactionRequest();
        newTransactionRequest.setAmount(1000.00);
        newTransactionRequest.setPaymentStatus("PAID");
        newTransactionRequest.setTransactionDate(LocalDate.now().plusDays(1));
        newTransactionRequest.setPaymentDate(null);
        newTransactionRequest.setDescription("Sales of 10 bags of maize");
        newTransactionRequest.setTransactionName("Maize sales");
        newTransactionRequest.setFinancialCategory("INCOME");
        // When
        mockMvc.perform(post("/api/finance/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTransactionRequest)))
        //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Pomyślnie dodano nową transakcję"));
    }
    @Test
    void testAddingExistingTransaction() throws Exception {
        //given
        Transaction existingTransaction= entityManager.find(Transaction.class, new TransactionId(1, 1));
NewTransactionRequest newTransactionRequest = new NewTransactionRequest();
newTransactionRequest.setAmount(existingTransaction.getAmount());
newTransactionRequest.setPaymentStatus(existingTransaction.getPaymentStatus().getName().toString());
newTransactionRequest.setTransactionDate(existingTransaction.getTransactionDate());
newTransactionRequest.setPaymentDate(existingTransaction.getPaymentDate());
newTransactionRequest.setDescription(existingTransaction.getDescription());
newTransactionRequest.setTransactionName(existingTransaction.getTransactionName());
newTransactionRequest.setFinancialCategory(existingTransaction.getFinancialCategory().getName().toString());

        //when
        mockMvc.perform(post("/api/finance/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTransactionRequest)))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transakcja o podanej nazwie już istnieje!"));
    }
	/*
	* PUT /{id}
	*/
	@Test
    public void testUpdateExistingTransaction() throws Exception {
        //given
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest();
        updateTransactionRequest.setAmount(2000.00);
        updateTransactionRequest.setPaymentStatus("PAID");
        updateTransactionRequest.setTransactionDate(LocalDate.now().plusDays(1));
        updateTransactionRequest.setPaymentDate(null);
        updateTransactionRequest.setDescription("Sales of 20 bags of maize");
        updateTransactionRequest.setTransactionName("Maize sales");
        updateTransactionRequest.setFinancialCategory("INCOME");

        //when
        mockMvc.perform(put("/api/finance/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTransactionRequest)))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pomyślnie zaktualizowano transakcję"));

        Transaction updatedTransaction = entityManager.find(Transaction.class, new TransactionId(1, 1));
        assertThat(updatedTransaction.getAmount(), is(2000.00));
        assertThat(updatedTransaction.getPaymentStatus().getName().toString(), is("PAID"));
        assertThat(updatedTransaction.getTransactionDate(), is(LocalDate.now().plusDays(1)));
    }

    @Test
    public void testUpdateFailedBecauseTransactionNameAlreadyExists() throws Exception {
        //given
        Transaction existingTransaction = entityManager.find(Transaction.class, new TransactionId(1,1));
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest();
        updateTransactionRequest.setAmount(2000.00);
        updateTransactionRequest.setPaymentStatus("PAID");
        updateTransactionRequest.setTransactionDate(LocalDate.now().plusDays(1));
        updateTransactionRequest.setPaymentDate(null);
        updateTransactionRequest.setDescription("Sales of 20 bags of maize");
        updateTransactionRequest.setTransactionName(existingTransaction.getTransactionName());
        updateTransactionRequest.setFinancialCategory("INCOME");

        //when
                mockMvc.perform(put("/api/finance/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTransactionRequest)))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transakcja o podanej nazwie już istnieje!"));
    }

    @Test
    public void testUpdateFailedDoesntAlreadyExist() throws Exception {
        //given
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest();
        updateTransactionRequest.setAmount(2000.00);
        updateTransactionRequest.setPaymentStatus("PAID");
        updateTransactionRequest.setTransactionDate(LocalDate.now().plusDays(1));
        updateTransactionRequest.setPaymentDate(null);
        updateTransactionRequest.setDescription("Sales of 20 bags of maize");
        updateTransactionRequest.setTransactionName("Cokolwkie");
        updateTransactionRequest.setFinancialCategory("INCOME");

        //when
                mockMvc.perform(put("/api/finance/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTransactionRequest)))
        //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transakcja nie została znaleziona"));
    }
    /*
     * DELETE /{id}
     */

    @Test
    public void testDeleteExisting() throws Exception {
       //when
       mockMvc.perform(delete("/api/finance/1"))
       //then
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.message").value("Transakcja została usunięta"));
       Transaction deletedTransaction = entityManager.find(Transaction.class, new TransactionId(1, 1));
       assertThat(deletedTransaction, is(nullValue()));
    }

    @Test
    public void testDeleteNonExistent() throws Exception {
        //given
        Integer nonExistentTransactionId = 999;
        //when
        mockMvc.perform(delete("/api/finance/" + nonExistentTransactionId))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Transakcja nie została znaleziona"));
    }

    /*
     * GET /all
     */

    @Test
    public void testGetAllTransactionWithoutFiltering() throws Exception {
        //given
        Farm currentFarm = entityManager.find(Farm.class, 1);
        Long transactionCount = entityManager.createQuery(
                        "SELECT COUNT(t) FROM Transaction t WHERE t.id.farmId = :farmId", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .getSingleResult();
    
        //when
        MvcResult result = mockMvc.perform(get("/api/finance/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    
        //then
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        List<TransactionDTO> transactionDTO = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<TransactionDTO>>() {
                });
    
        assertThat(transactionDTO.size(), is(transactionCount.intValue()));
    }

    @Test
    public void testGetAllTransactionWithDateFilter() throws Exception {
        //given
        Farm currentFarm = entityManager.find(Farm.class, 1);
        Long transactionCount = entityManager.createQuery(
                        "SELECT COUNT(t) FROM Transaction t WHERE t.id.farmId = :farmId AND t.transactionDate >= :minDate AND t.transactionDate <= :maxDate", Long.class)
                .setParameter("farmId", currentFarm.getId())
                .setParameter("minDate", LocalDate.now().minusYears(3))
                .setParameter("maxDate", LocalDate.now())
                .getSingleResult();

        //when
        MvcResult result = mockMvc.perform(get("/api/finance/all")
                        .param("minDate", LocalDate.now().minusYears(3).toString())
                        .param("maxDate", LocalDate.now().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        List<TransactionDTO> transactionDTO = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<TransactionDTO>>() {
                });

        assertThat(transactionDTO.size(), is(transactionCount.intValue()));
    }

    /*
     * GET /balance
     */

    @Test
    public void testGetBalance() throws Exception {
        //when
        MvcResult result = mockMvc.perform(get("/api/finance/balance"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        BalanceResponse balanceResponse = objectMapper.readValue(result.getResponse().getContentAsString(), BalanceResponse.class);
        assertThat(balanceResponse, is(notNullValue()));
    }


}
