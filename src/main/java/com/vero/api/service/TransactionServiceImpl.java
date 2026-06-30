package com.vero.api.service;

import com.vero.api.dto.TransactionRequest;
import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import com.vero.api.repository.TransactionRepository;
import com.vero.api.util.BudgetCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        if(id == null){
            throw new IllegalArgumentException("id must not be null");
        }
        return repository.findById(id);
    }

    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId) {
        if(accountId == null){
            throw new IllegalArgumentException("accountId must not be null");
        }
        return repository.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return repository.findByTransactionDateBetween(startDate, endDate);
    }

    @Override
    public Transaction createTransaction(TransactionRequest request) {
        if(request == null){
            throw new IllegalArgumentException("request must not be null");
        }
        
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .build();

        return repository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        if(id == null){
            throw new IllegalArgumentException("id must not be null");
        }
        repository.deleteById(id);
    }

    @Override
    public Map<Category, BigDecimal> calculateMonthlySpend(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        return repository.findAll().stream()
                .filter(t -> !t.getTransactionDate().isBefore(startOfMonth)
                        && !t.getTransactionDate().isAfter(endOfMonth))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory(),
                        Collectors.reducing(
                            BigDecimal.ZERO, 
                            t->t.getAmount(), 
                            (a,b) -> a.add(b)
                        )
                ));
    }

    @Override
    public Map<Category, BigDecimal> getTopSpendingCategories(List<Transaction> transactions, int topN) {
        return BudgetCalculator.getTopSpendingCategories(transactions, topN);
    }

    // public List<Transaction> getCategoryTransactionsForMonth(Category category, int year, int month) {
    //     return repository.findByCategoryAndMonth(category, year, month);
    // }
}
