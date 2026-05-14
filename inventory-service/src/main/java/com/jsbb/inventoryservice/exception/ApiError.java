package com.jsbb.inventoryservice.exception;

public record ApiError(String status, String title, String detail) {}
