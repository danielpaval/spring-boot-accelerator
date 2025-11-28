package com.example.demo.exception;

import com.example.demo.generated.dto.ErrorResponse;
import com.example.demo.generated.dto.Error;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status,
        WebRequest request) {
        List<Error> errors = new ArrayList<>();

        // Add field errors
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Error error = Error.builder()
                .detail(fieldError.getDefaultMessage())
                .pointer("/".concat(fieldError.getField()))
                .code(fieldError.getCode())
                .build();
            errors.add(error);
        }

        // Add global errors
        for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
            Error error = Error.builder()
                .detail(globalError.getDefaultMessage())
                .code(globalError.getCode())
                .build();
            errors.add(error);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .title("Validation Failed")
            .detail("Request validation failed with " + errors.size() + " error(s)")
            .timestamp(OffsetDateTime.now().toString())
            .errors(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}

