package com.quanna.domain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for CartItem entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private ProductDTO product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Calculate subtotal (unit price * quantity)
     */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calculate total after discount
     */
    public BigDecimal getTotal() {
        BigDecimal subtotal = getSubtotal();
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return subtotal.subtract(discountAmount);
        }
        return subtotal;
    }
}

