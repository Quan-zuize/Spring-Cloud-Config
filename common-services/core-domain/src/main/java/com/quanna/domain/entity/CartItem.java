package com.quanna.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * CartItem entity representing an item in a shopping cart
 * Optimized for PostgreSQL database
 */
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_user_id", columnList = "user_id"),
    @Index(name = "idx_cart_product_id", columnList = "product_id"),
    @Index(name = "idx_cart_deleted", columnList = "is_deleted"),
    @Index(name = "idx_cart_created_at", columnList = "created_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_product", columnNames = {"user_id", "product_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Product ID is required")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_cart_item_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_cart_item_product"))
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    @Builder.Default
    private Integer quantity = 1;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Unit price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2, columnDefinition = "NUMERIC(12,2)")
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 12, scale = 2, columnDefinition = "NUMERIC(12,2) DEFAULT 0")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

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

    /**
     * Increase quantity by given amount
     */
    public void increaseQuantity(Integer amount) {
        if (amount != null && amount > 0) {
            this.quantity += amount;
        }
    }

    /**
     * Decrease quantity by given amount
     */
    public void decreaseQuantity(Integer amount) {
        if (amount != null && amount > 0) {
            this.quantity = Math.max(1, this.quantity - amount);
        }
    }

    /**
     * Update unit price from product
     */
    public void updatePriceFromProduct(Product product) {
        if (product != null) {
            this.unitPrice = product.getEffectivePrice();
        }
    }
}

