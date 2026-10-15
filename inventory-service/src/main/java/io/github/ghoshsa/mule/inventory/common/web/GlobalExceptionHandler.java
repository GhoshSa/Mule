package io.github.ghoshsa.mule.inventory.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.github.ghoshsa.mule.inventory.exception.ProductNotFound;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler (ProductNotFound.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFound exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(404, "PRODUCT_NOT_FOUND", exception.getMessage(), extractPath(request)));
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}