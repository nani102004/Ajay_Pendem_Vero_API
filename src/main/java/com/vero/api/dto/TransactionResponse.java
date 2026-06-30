package com.vero.api.dto;

import com.vero.api.model.Transaction;
import com.vero.api.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Outbound response payload representing a transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String description;
    private Category category;
    private LocalDate transactionDate;

    public static TransactionResponse fromTransaction(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccountId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .category(t.getCategory())
                .transactionDate(t.getTransactionDate())
                .build();
    }
}
