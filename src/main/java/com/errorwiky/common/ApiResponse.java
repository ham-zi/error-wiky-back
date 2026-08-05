package com.errorwiky.common;

public record ApiResponse<T>(boolean success, T data, String message) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data, null); }
    public static ApiResponse<Void> ok(String message) { return new ApiResponse<>(true, null, message); }
    public static ApiResponse<Void> fail(String message) { return new ApiResponse<>(false, null, message); }
}
