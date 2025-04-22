package com.api.app_location.exception;

public class FailedSaveException extends RuntimeException {
    public FailedSaveException(String message) {
        super(message);
    }
}
