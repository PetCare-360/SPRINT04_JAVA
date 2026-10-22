package br.com.fiap.petcare360_java.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import br.com.fiap.petcare360_java.controller.AdminWebController;
import br.com.fiap.petcare360_java.controller.ClinicalWebController;
import br.com.fiap.petcare360_java.controller.LanguageController;
import br.com.fiap.petcare360_java.controller.PublicWebController;
import br.com.fiap.petcare360_java.controller.TutorWebController;
import br.com.fiap.petcare360_java.controller.VetWebController;
import br.com.fiap.petcare360_java.controller.WebFormController;

@ControllerAdvice(assignableTypes = { AdminWebController.class, ClinicalWebController.class,
		LanguageController.class, PublicWebController.class, TutorWebController.class,
		VetWebController.class, WebFormController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);
	private final MessageSource messages;

	public WebExceptionHandler(MessageSource messages) {
		this.messages = messages;
	}

	@ExceptionHandler(ApiException.class)
	public ModelAndView handleApi(ApiException ex) {
		return page(ex.getStatus(), ex.getMessage());
	}

	@ExceptionHandler(DataAccessException.class)
	public ModelAndView handleDatabase(DataAccessException ex) {
		log.error("Falha de persistência em página MVC", ex);
		return page(HttpStatus.INTERNAL_SERVER_ERROR, message("error.database"));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ModelAndView handleStatus(ResponseStatusException ex) {
		return page(HttpStatus.valueOf(ex.getStatusCode().value()), message("error.invalidPayload"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ModelAndView handleDenied(AccessDeniedException ex) {
		return page(HttpStatus.FORBIDDEN, message("error.clinicalForbidden"));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ModelAndView handleAuthentication(AuthenticationException ex) {
		return page(HttpStatus.UNAUTHORIZED, message("error.invalidCredentials"));
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleUnexpected(Exception ex) {
		log.error("Falha inesperada em página MVC", ex);
		return page(HttpStatus.INTERNAL_SERVER_ERROR, message("error.internal"));
	}

	private String message(String key) {
		return messages.getMessage(key, null, LocaleContextHolder.getLocale());
	}

	private ModelAndView page(HttpStatus status, String text) {
		ModelAndView view = new ModelAndView("error-page");
		view.setStatus(status);
		view.addObject("statusCode", status.value());
		view.addObject("errorMessage", text);
		return view;
	}
}
