package com.portfolio.backend.common.exception.handler;

import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class BadRequestErrorResponse extends ErrorResponse {

    private final List<Error> errors;

    public BadRequestErrorResponse(LocalDateTime timestamp, int status, String message, String details, BindingResult bindingResult) {
        super(timestamp, status, message, details);

        Map<String, List<String>> fieldErrorMap = bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                FieldError::getDefaultMessage,
                                Collectors.toList()
                        )
                ));

		this.errors = fieldErrorMap.entrySet().stream()
				.map(entry -> new Error(entry.getKey(), entry.getValue()))
				.toList();
    }

    public BadRequestErrorResponse(LocalDateTime timestamp, int status, String message, String details, List<Error> errors) {
        super(timestamp, status, message, details);
        this.errors = errors;
    }

    public record Error(String field, List<String> messages) {}
}
