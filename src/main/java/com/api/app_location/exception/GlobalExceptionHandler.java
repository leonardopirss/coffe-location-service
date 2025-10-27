package com.api.app_location.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(FailedSaveException.class)
  public ResponseEntity<?> handleFailedSalve(FailedSaveException e) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("ERROR", e.getMessage()));
  }

    @ExceptionHandler(FailedFilterData.class)
    public ResponseEntity<?> handleFailedFind(FailedSaveException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ERROR", e.getMessage()));
    }
}