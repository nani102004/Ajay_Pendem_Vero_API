package com.vero.api.service;

import com.vero.api.dto.TransactionRequest;
import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import com.vero.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionServiceImpl service;

    private List<Transaction> sampleTransactions;

    private Transaction firstOfDecember;

    @BeforeEach
    void setUp() {
        firstOfDecember = Transaction.builder()
                .id(1L)
                .accountId(1L)
                .amount(new BigDecimal("87.45"))
                .description("Whole Foods Market")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2024, 12, 1))
                .build();

        Transaction midDecemberTransport = Transaction.builder()
                .id(2L)
                .accountId(1L)
                .amount(new BigDecimal("42.00"))
                .description("Uber ride to airport")
                .category(Category.TRANSPORT)
                .transactionDate(LocalDate.of(2024, 12, 3))
                .build();

        Transaction midDecemberUtilities = Transaction.builder()
                .id(3L)
                .accountId(1L)
                .amount(new BigDecimal("124.50"))
                .description("Electric bill")
                .category(Category.UTILITIES)
                .transactionDate(LocalDate.of(2024, 12, 5))
                .build();

        Transaction midDecemberFood = Transaction.builder()
                .id(4L)
                .accountId(2L)
                .amount(new BigDecimal("56.78"))
                .description("Trader Joes")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2024, 12, 14))
                .build();

        Transaction lateDecemberEntertainment = Transaction.builder()
                .id(5L)
                .accountId(1L)
                .amount(new BigDecimal("28.50"))
                .description("Movie tickets")
                .category(Category.ENTERTAINMENT)
                .transactionDate(LocalDate.of(2024, 12, 22))
                .build();

        Transaction novemberFood = Transaction.builder()
                .id(6L)
                .accountId(1L)
                .amount(new BigDecimal("73.00"))
                .description("Whole Foods Market")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2024, 11, 5))
                .build();

        Transaction novemberTransport = Transaction.builder()
                .id(7L)
                .accountId(1L)
                .amount(new BigDecimal("22.50"))
                .description("Uber across town")
                .category(Category.TRANSPORT)
                .transactionDate(LocalDate.of(2024, 11, 18))
                .build();

        sampleTransactions = List.of(
                firstOfDecember,
                midDecemberTransport,
                midDecemberUtilities,
                midDecemberFood,
                lateDecemberEntertainment,
                novemberFood,
                novemberTransport
        );
    }

    @Test
    void testGetAllTransactions_returnsAllRecords() {
        // Verifies the service returns every record the repository hands back.
        when(repository.findAll()).thenReturn(sampleTransactions);

        List<Transaction> result = service.getAllTransactions();

        assertThat(result).hasSize(7).containsExactlyElementsOf(sampleTransactions);
    }

    @Test
    void testGetTransactionById_whenExists_returnsTransaction() {
        // Verifies that a lookup by ID returns the wrapped transaction when present.
        when(repository.findById(1L)).thenReturn(Optional.of(firstOfDecember));

        Optional<Transaction> result = service.getTransactionById(1L);

        assertThat(result).isPresent().contains(firstOfDecember);
    }

    @Test
    void testGetTransactionById_whenNotFound_returnsEmpty() {
        // Verifies that a missing ID returns an empty Optional rather than throwing.
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Transaction> result = service.getTransactionById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetTransactionsByAccount_returnsCorrectSubset() {
        // Verifies the service delegates to the derived repository query for account-scoped reads.
        List<Transaction> accountOne = sampleTransactions.stream()
                .filter(t -> t.getAccountId().equals(1L))
                .toList();
        when(repository.findByAccountId(1L)).thenReturn(accountOne);

        List<Transaction> result = service.getTransactionsByAccount(1L);

        assertThat(result).hasSize(6).allMatch(t -> t.getAccountId().equals(1L));
    }

    @Test
    void testCreateTransaction_persistsAndReturns() {
        // Verifies the service maps a request into an entity and persists it.
        TransactionRequest request = TransactionRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("19.99"))
                .description("Spotify Premium")
                .category(Category.ENTERTAINMENT)
                .transactionDate(LocalDate.of(2024, 12, 15))
                .build();

        Transaction saved = Transaction.builder()
                .id(42L)
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .build();

        when(repository.save(any(Transaction.class))).thenReturn(saved);

        Transaction result = service.createTransaction(request);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getAmount()).isEqualByComparingTo("19.99");
        assertThat(result.getCategory()).isEqualTo(Category.ENTERTAINMENT);
        verify(repository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_whenRequestIsNull_throwsException() {
        // Verified create transaction when transaction is null
        assertThatThrownBy(() -> service.createTransaction(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("request must not be null");

        verify(repository, never()).save(any(Transaction.class));
    }

    @Test
    void testCalculateMonthlySpend_includesFirstDayOfMonth() {
        // Catches the off-by-one bug: transactions dated on the 1st of the target month
        // must be included in the result.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        assertThat(result).containsKey(Category.FOOD);
        // FOOD on Dec 1 (87.45) plus FOOD on Dec 14 (56.78) = 144.23.
        // If the boundary excludes the 1st, this assertion fails because FOOD would only be 56.78.
        assertThat(result.get(Category.FOOD)).isEqualByComparingTo("144.23");
    }

    @Test
    void testCalculateMonthlySpend_excludesOtherMonths() {
        // Verifies that transactions outside the target month are not summed in.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        // November TRANSPORT was 22.50; December TRANSPORT was 42.00. Only December should count.
        assertThat(result.get(Category.TRANSPORT)).isEqualByComparingTo("42.00");
    }

    @Test
    void testCalculateMonthlySpend_sumsCorrectlyByCategory() {
        // Verifies grouping and summing logic across multiple categories within the same month.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        assertThat(result.get(Category.UTILITIES)).isEqualByComparingTo("124.50");
        assertThat(result.get(Category.ENTERTAINMENT)).isEqualByComparingTo("28.50");
    }

    @Test
    void testGetTopSpendingCategories_returnsTopN() {
        // Verifies the service surfaces the top N categories from a list of transactions.
        Map<Category, BigDecimal> result = service.getTopSpendingCategories(sampleTransactions, 3);

        assertThat(result).hasSizeLessThanOrEqualTo(3);
        // The highest-spend category over the sample data should be UTILITIES (124.50)
        // since it's a single large transaction. FOOD totals more across rows.
        assertThat(result.keySet()).contains(Category.FOOD);
    }

    @Test
    void testGetTopSpendingCategories_respectsTopNLimit() {
        // Verifies the returned map never exceeds the topN limit.
        Map<Category, BigDecimal> result = service.getTopSpendingCategories(sampleTransactions, 2);

        assertThat(result).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void testDeleteTransaction_deletesById() {
        service.deleteTransaction(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTransaction_whenIdIsNull_throwsException() {
        assertThatThrownBy(() -> service.deleteTransaction(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be null");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void testGetTransactionsByDateRange_returnsTransactionsWithinRange() {
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 14);

        List<Transaction> expected = sampleTransactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate)
                        && !t.getTransactionDate().isAfter(endDate))
                .toList();

        when(repository.findByTransactionDateBetween(startDate, endDate))
                .thenReturn(expected);

        List<Transaction> result = service.getTransactionsByDateRange(startDate, endDate);

        assertThat(result)
                .hasSize(4)
                .allMatch(t -> !t.getTransactionDate().isBefore(startDate)
                        && !t.getTransactionDate().isAfter(endDate));

        verify(repository, times(1))
                .findByTransactionDateBetween(startDate, endDate);
    }

    @Test
    void testGetTransactionsByDateRange_whenNoTransactions_returnsEmptyList() {
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 31);

        when(repository.findByTransactionDateBetween(startDate, endDate))
                .thenReturn(List.of());

        List<Transaction> result = service.getTransactionsByDateRange(startDate, endDate);

        assertThat(result).isEmpty();

        verify(repository, times(1))
                .findByTransactionDateBetween(startDate, endDate);
    }
}
