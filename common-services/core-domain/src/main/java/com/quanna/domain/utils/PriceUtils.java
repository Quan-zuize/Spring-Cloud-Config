package com.quanna.domain.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for price calculations
 */
public class PriceUtils {

    private PriceUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Round price to 2 decimal places
     */
    public static BigDecimal round(BigDecimal price) {
        return price != null ? price.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING) : BigDecimal.ZERO;
    }

    /**
     * Calculate discount amount
     */
    public static BigDecimal calculateDiscount(BigDecimal price, BigDecimal discountPercent) {
        if (price == null || discountPercent == null) {
            return BigDecimal.ZERO;
        }
        return round(price.multiply(discountPercent).divide(BigDecimal.valueOf(100), DEFAULT_ROUNDING));
    }

    /**
     * Calculate final price after discount
     */
    public static BigDecimal applyDiscount(BigDecimal price, BigDecimal discountPercent) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = calculateDiscount(price, discountPercent);
        return round(price.subtract(discount));
    }

    /**
     * Calculate tax amount
     */
    public static BigDecimal calculateTax(BigDecimal price, BigDecimal taxPercent) {
        if (price == null || taxPercent == null) {
            return BigDecimal.ZERO;
        }
        return round(price.multiply(taxPercent).divide(BigDecimal.valueOf(100), DEFAULT_ROUNDING));
    }

    /**
     * Calculate total with tax
     */
    public static BigDecimal applyTax(BigDecimal price, BigDecimal taxPercent) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal tax = calculateTax(price, taxPercent);
        return round(price.add(tax));
    }
}

