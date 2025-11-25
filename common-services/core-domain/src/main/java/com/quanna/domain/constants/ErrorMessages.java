package com.quanna.domain.constants;

/**
 * Error message constants
 */
public class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    // User errors
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User with this email already exists";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String USER_INACTIVE = "User account is inactive";

    // Product errors
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String PRODUCT_OUT_OF_STOCK = "Product is out of stock";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock quantity";

    // Cart errors
    public static final String CART_ITEM_NOT_FOUND = "Cart item not found";
    public static final String INVALID_QUANTITY = "Invalid quantity";

    // Validation errors
    public static final String INVALID_EMAIL = "Invalid email format";
    public static final String INVALID_PHONE = "Invalid phone number format";
    public static final String REQUIRED_FIELD = "This field is required";
    public static final String INVALID_PRICE = "Price must be greater than 0";

    // Authorization errors
    public static final String UNAUTHORIZED_ACCESS = "You are not authorized to access this resource";
    public static final String ACCESS_DENIED = "Access denied";
}

