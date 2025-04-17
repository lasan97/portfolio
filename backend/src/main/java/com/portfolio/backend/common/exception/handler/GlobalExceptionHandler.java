package com.portfolio.backend.common.exception.handler;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.FileStorageException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BadRequestErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new BadRequestErrorResponse(
						LocalDateTime.now(),
						HttpStatus.NOT_FOUND.value(),
						ex.getMessage(),
						request.getDescription(false),
						ex.getBindingResult()));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<BadRequestErrorResponse> handleBindException(BindException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new BadRequestErrorResponse(
						LocalDateTime.now(),
						HttpStatus.NOT_FOUND.value(),
						ex.getMessage(),
						request.getDescription(false),
						ex.getBindingResult()));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
			ResourceNotFoundException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(
								LocalDateTime.now(),
								HttpStatus.NOT_FOUND.value(),
								ex.getMessage(),
								request.getDescription(false)));
	}

	@ExceptionHandler(UnprocessableEntityException.class)
	public ResponseEntity<ErrorResponse> handleUnprocessableEntityException(
			UnprocessableEntityException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(new ErrorResponse(
						LocalDateTime.now(),
						HttpStatus.UNPROCESSABLE_ENTITY.value(),
						ex.getMessage(),
						request.getDescription(false)));
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						LocalDateTime.now(),
						HttpStatus.NOT_FOUND.value(),
						ex.getMessage(),
						request.getDescription(false)));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(new ErrorResponse(
						LocalDateTime.now(),
						HttpStatus.PAYLOAD_TOO_LARGE.value(),
						ex.getMessage(),
						request.getDescription(false)));
	}

	@ExceptionHandler(FileStorageException.class)
	public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						LocalDateTime.now(),
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						ex.getMessage(),
						request.getDescription(false)));
	}
}
