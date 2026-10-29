package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.fiap.petcare360_java.exception.GlobalExceptionHandler;

class FaviconRequestTest {
	@Test
	void missingFaviconReturnsNoContentWithoutBody() {
		var messages = new StaticMessageSource();
		messages.setUseCodeAsDefaultMessage(true);
		var handler = new GlobalExceptionHandler(messages);
		var request = new MockHttpServletRequest("GET", "/favicon.ico");
		var exception = new NoResourceFoundException(HttpMethod.GET, "/favicon.ico", "favicon.ico");

		var response = handler.handleNoResource(exception, request);

		assertEquals(204, response.getStatusCode().value());
		assertNull(response.getBody());
	}
}
