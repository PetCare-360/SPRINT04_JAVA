package br.com.fiap.petcare360_java.exception;

import java.time.OffsetDateTime;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private final MessageSource messages;

	public GlobalExceptionHandler(MessageSource messages) {
		this.messages = messages;
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
		return build(ex.getStatus(), ex.getMessage(), request, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		Map<String, String> fields = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
		return build(HttpStatus.BAD_REQUEST, message("error.invalidPayload"), request, fields);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, message("error.invalidCredentials"), request, null);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDatabase(DataAccessException ex, HttpServletRequest request) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof SQLException sql && sql.getErrorCode() >= 20700 && sql.getErrorCode() <= 20799) {
				return switch (sql.getErrorCode()) {
					case 20701, 20714 -> build(HttpStatus.NOT_FOUND, message("error.notFound"), request, null);
					case 20706, 20716 -> build(HttpStatus.CONFLICT, message("error.clinicalConflict"), request, null);
					case 20708, 20715 -> build(HttpStatus.FORBIDDEN, message("error.clinicalForbidden"), request, null);
					default -> build(HttpStatus.BAD_REQUEST, message("error.clinicalInvalid"), request, null);
				};
			}
		}
		log.error("Falha de persistência em {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, message("error.database"), request, null);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<?> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
		String requestPath = request.getRequestURI().substring(request.getContextPath().length());
		boolean faviconRequest = "/favicon.ico".equals(requestPath)
				&& ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod()));

		if (faviconRequest) {
			return ResponseEntity.noContent().build();
		}

		return handleUnexpected(ex, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Falha inesperada em {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, message("error.internal"), request, null);
	}

	private String message(String key) {
		return messages.getMessage(key, null, LocaleContextHolder.getLocale());
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
			Map<String, String> fields) {
		ErrorResponse response = new ErrorResponse(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fields);

		return ResponseEntity.status(status).body(response);
	}
}
