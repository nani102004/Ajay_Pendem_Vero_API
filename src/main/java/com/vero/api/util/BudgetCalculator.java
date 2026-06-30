package com.vero.api.util;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BudgetCalculator {

    private BudgetCalculator() {
    }

    /**
     * Groups the provided transactions by category, sums the total amount spent
     * per category, and returns the top N categories by total spend in descending order.
     *
     * The returned map preserves insertion order and contains at most {@code topN} entries.
     * If there are fewer distinct categories than {@code topN}, all categories are returned.
     *
     * @param transactions the list of transactions to analyse; must not be null
     * @param topN         the maximum number of categories to return; must be greater than zero
     * @return a map of Category to total spend, sorted descending by spend, limited to topN entries
     */
    public static Map<Category, BigDecimal> getTopSpendingCategories(List<Transaction> transactions, int topN) {
        if(transactions == null){
            throw new IllegalArgumentException("transactions must not be null");
        }

        if(topN <= 0){
            throw new IllegalArgumentException("topN must be greater than 0");
        }

        Map<Category,BigDecimal>totalsByCategory = transactions.stream()
            .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory(),
                        Collectors.reducing(
                            BigDecimal.ZERO,
                            transaction -> transaction.getAmount(),
                            (amount1,amount2) -> amount1.add(amount2)
                    )
            ));

        return totalsByCategory.entrySet().stream()
                    .sorted((entry1,entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                    .limit(topN)
                    .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue(),
                        (existing,replacement) -> existing,
                        LinkedHashMap::new
                    ));

        
    }
}
