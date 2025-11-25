package com.quanna.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for shopping cart summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummaryDTO {

    private Long userId;

    private List<CartItemDTO> items;

    private Integer totalItems;

    private BigDecimal subtotal;

    private BigDecimal totalDiscount;

    private BigDecimal total;

    /**
     * Calculate totals from cart items
     */
    public void calculateTotals() {
        if (items == null || items.isEmpty()) {
            totalItems = 0;
            subtotal = BigDecimal.ZERO;
            totalDiscount = BigDecimal.ZERO;
            total = BigDecimal.ZERO;
            return;
        }

        totalItems = items.stream()
            .mapToInt(CartItemDTO::getQuantity)
            .sum();

        subtotal = items.stream()
            .map(CartItemDTO::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalDiscount = items.stream()
            .map(item -> item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        total = items.stream()
            .map(CartItemDTO::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

