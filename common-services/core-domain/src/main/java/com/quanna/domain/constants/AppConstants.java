package com.quanna.domain.constants;

/**
 * Common constants used across the application
 */
public final class AppConstants {
    // Validation
    public static final int MAX_DESCRIPTION_LENGTH = 5000;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MIN_PASSWORD_LENGTH = 8;

    // API
    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String API_VERSION = "/api/v1";

    // Cache
    public static final long CACHE_TTL_MINUTES = 30;
    public static final String CACHE_CART_ITEMS = "cartItems";
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_USERS = "users";

    // Date Time
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";

    // Pagination
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_PAGE_NUMBER = "0";

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
