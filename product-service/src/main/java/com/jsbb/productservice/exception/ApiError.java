package com.jsbb.productservice.exception;

public record ApiError(String status, String title, String detail) {}
