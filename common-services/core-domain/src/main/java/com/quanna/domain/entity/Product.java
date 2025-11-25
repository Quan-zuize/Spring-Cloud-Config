package com.quanna.domain.entity;

import com.quanna.domain.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Product entity representing a product in the e-commerce system
 * Optimized for PostgreSQL database
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_deleted", columnList = "is_deleted"),
    @Index(name = "idx_product_created_at", columnList = "created_at"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_stock", columnList = "stock_quantity")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank(message = "SKU is required")
    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "price", nullable = false, precision = 12, scale = 2, columnDefinition = "NUMERIC(12,2)")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Discount price must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Discount price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "discount_price", precision = 12, scale = 2, columnDefinition = "NUMERIC(12,2)")
    private BigDecimal discountPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "weight", precision = 10, scale = 2, columnDefinition = "NUMERIC(10,2)")
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Min(value = 0, message = "Minimum order quantity cannot be negative")
    @Column(name = "min_order_quantity", columnDefinition = "INTEGER DEFAULT 1")
    @Builder.Default
    private Integer minOrderQuantity = 1;

    @Min(value = 0, message = "Maximum order quantity cannot be negative")
    @Column(name = "max_order_quantity")
    private Integer maxOrderQuantity;

    /**
     * Check if product is available for purchase
     */
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stockQuantity > 0;
    }

    /**
     * Get effective price (discount price if available, otherwise regular price)
     */
    public BigDecimal getEffectivePrice() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0 
            ? discountPrice 
            : price;
    }

    /**
     * Check if product has discount
     */
    public boolean hasDiscount() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
            && discountPrice.compareTo(price) < 0;
    }

    /**
     * Check if requested quantity is available
     */
    public boolean hasStock(Integer quantity) {
        return stockQuantity >= quantity;
    }
}

