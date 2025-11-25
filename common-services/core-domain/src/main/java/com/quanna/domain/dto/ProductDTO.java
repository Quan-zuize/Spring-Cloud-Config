package com.quanna.domain.dto;

import com.quanna.domain.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Product entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Discount price must be 0 or greater")
    private BigDecimal discountPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String category;

    private String brand;

    private String imageUrl;

    private ProductStatus status;

    private BigDecimal weight;

    private String dimensions;

    private Integer minOrderQuantity;

    private Integer maxOrderQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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
}

