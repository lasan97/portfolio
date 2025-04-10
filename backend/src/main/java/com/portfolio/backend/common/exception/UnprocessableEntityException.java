package com.portfolio.backend.common.exception;

public class UnprocessableEntityException extends RuntimeException {

	public UnprocessableEntityException(String message) {
		super(message);
	}

	public UnprocessableEntityException(String message, Throwable cause) {
		super(message, cause);
	}
}
