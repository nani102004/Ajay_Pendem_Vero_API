package com.vero.api;

/**
 * TransactionCandidateTest.java
 *
 * This is your file. Write your tests here.
 *
 * The existing test coverage in this project is incomplete. Your job is to
 * add meaningful tests that verify the behaviour of the code you have written
 * or fixed.
 *
 * What that means:
 *   - Tests should validate real behaviour, not just assert that methods exist.
 *   - Consider testing at the controller level (MockMvc) as well as the service level.
 *   - Think about edge cases: what happens with an empty transaction list?
 *     What happens when a month has no transactions?
 *   - Tests must compile and pass cleanly when you submit.
 *
 * What good looks like:
 *   A test that would catch a regression if someone changed the logic you wrote.
 *   Not a test that exists to satisfy a requirement.
 *
 * AI tools are permitted and expected. Document how you used them in DECISIONS.md,
 * including what the tool got wrong or what you changed.
 */

import com.vero.api.controller.TransactionController;
import com.vero.api.dto.TransactionRequest;
import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import com.vero.api.service.TransactionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionCandidateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @Test
    void getAllTransactions_shouldReturnTransactionResponses() throws Exception{
        // Verified get all trandactions with success scenario
        Transaction transaction = Transaction.builder()
            .id(1L)
            .accountId(1L)
            .amount(new BigDecimal("87.45"))
            .description("Whole Foods Market")
            .category(Category.FOOD)
            .transactionDate(LocalDate.of(2024, 12, 1))
            .build();

        when(service.getAllTransactions()).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(1))
                .andExpect(jsonPath("$[0].amount").value(87.45))
                .andExpect(jsonPath("$[0].description").value("Whole Foods Market"))
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].transactionDate").value("2024-12-01"));

        verify(service).getAllTransactions();

    }

    @Test
    void getTransactionById_shouldReturnTransactionResponse() throws Exception{
        // Verified get trandaction By id with success scenario
        Transaction transaction = Transaction.builder()
            .id(1L)
            .accountId(1L)
            .amount(new BigDecimal("87.45"))
            .description("Whole Foods Market")
            .category(Category.FOOD)
            .transactionDate(LocalDate.of(2024, 12, 1))
            .build();

        when(service.getTransactionById(1L)).thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.amount").value(87.45))
                .andExpect(jsonPath("$.description").value("Whole Foods Market"))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.transactionDate").value("2024-12-01"));

        verify(service).getTransactionById(1L);

    }

    @Test
    void getTransactionById_whenNotFound_shouldReturn404() throws Exception {
        // Verified get trandaction By id with not found scenario
        when(service.getTransactionById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound());

        verify(service).getTransactionById(999L);
    }


    @Test
    void getTransactionsByAccount_shouldReturnTransactionResponses() throws Exception{
        // Verified get trandaction By account with success scenario
        Transaction transaction1 = Transaction.builder()
            .id(1L)
            .accountId(1L)
            .amount(new BigDecimal("87.45"))
            .description("Whole Foods Market")
            .category(Category.FOOD)
            .transactionDate(LocalDate.of(2024, 12, 1))
            .build();

        Transaction transaction2 = Transaction.builder()
            .id(2L)
            .accountId(1L)
            .amount(new BigDecimal("60.45"))
            .description("Whole Foods Super Market")
            .category(Category.FOOD)
            .transactionDate(LocalDate.of(2025, 12, 1))
            .build();

        when(service.getTransactionsByAccount(1L)).thenReturn(List.of(transaction1,transaction2));

        mockMvc.perform(get("/api/transactions/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountId").value(1))
                .andExpect(jsonPath("$[0].amount").value(87.45))
                .andExpect(jsonPath("$[0].description").value("Whole Foods Market"))
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].transactionDate").value("2024-12-01"));

        verify(service).getTransactionsByAccount(1L);

    }

    @Test
    void getTransactionsByAccount_whenNoTransactions_shouldReturnEmptyList() throws Exception {
        // Verified get trandaction By account with out data
        when(service.getTransactionsByAccount(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions/account/999"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(service).getTransactionsByAccount(999L);
    }

    @Test
    void getMonthlySpend_shouldReturnSpendByCategory() throws Exception {
        // Verified get trandaction By category
        Map<Category, BigDecimal> monthlySpend = Map.of(
                Category.FOOD, new BigDecimal("236.53"),
                Category.TRANSPORT, new BigDecimal("90.00")
        );

        when(service.calculateMonthlySpend(2024, 12)).thenReturn(monthlySpend);

        mockMvc.perform(get("/api/transactions/monthly-spend")
                        .param("year", "2024")
                        .param("month", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FOOD").value(236.53))
                .andExpect(jsonPath("$.TRANSPORT").value(90.00));

        verify(service).calculateMonthlySpend(2024, 12);
    }
    
    @Test
    void getMonthlySpend_whenNoTransactions_shouldReturnEmptyMap() throws Exception {
        // Verified get trandaction By montly spend
        when(service.calculateMonthlySpend(2030, 1)).thenReturn(Map.of());

        mockMvc.perform(get("/api/transactions/monthly-spend")
                        .param("year", "2030")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(service).calculateMonthlySpend(2030, 1);
    }

    @Test
    void deleteTransaction_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).deleteTransaction(1L);
    }

    @Test
    void createTransaction_shouldReturnCreatedTransactionResponse() throws Exception {
        // Verified create transaction
        Transaction created = Transaction.builder()
                .id(42L)
                .accountId(1L)
                .amount(new BigDecimal("19.99"))
                .description("Spotify Premium")
                .category(Category.ENTERTAINMENT)
                .transactionDate(LocalDate.of(2024, 12, 15))
                .build();

        when(service.createTransaction(any(TransactionRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "accountId": 1,
                                "amount": 19.99,
                                "description": "Spotify Premium",
                                "category": "ENTERTAINMENT",
                                "transactionDate": "2024-12-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.amount").value(19.99))
                .andExpect(jsonPath("$.description").value("Spotify Premium"))
                .andExpect(jsonPath("$.category").value("ENTERTAINMENT"))
                .andExpect(jsonPath("$.transactionDate").value("2024-12-15"));

        verify(service).createTransaction(any(TransactionRequest.class));
    }

    

}
