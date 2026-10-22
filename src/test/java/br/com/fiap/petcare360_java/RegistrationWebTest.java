package br.com.fiap.petcare360_java;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import br.com.fiap.petcare360_java.controller.PublicWebController;
import br.com.fiap.petcare360_java.service.AuthService;
import br.com.fiap.petcare360_java.service.CurrentUserService;

class RegistrationWebTest {
	@Test
	void registrationPageRendersBothRoles() throws Exception {
		var resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML");
		var engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		var messages = new ResourceBundleMessageSource();
		messages.setBasename("messages");
		engine.setTemplateEngineMessageSource(messages);
		var view = new ThymeleafViewResolver();
		view.setTemplateEngine(engine);
		var mvc = MockMvcBuilders.standaloneSetup(new PublicWebController(mock(AuthService.class),
				mock(CurrentUserService.class))).setViewResolvers(view).build();

		String html = mvc.perform(get("/register")
				.requestAttr("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test")))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		org.junit.jupiter.api.Assertions.assertTrue(html.contains("ROLE_CLIENTE"));
		org.junit.jupiter.api.Assertions.assertTrue(html.contains("ROLE_VETERINARIO"));
		org.junit.jupiter.api.Assertions.assertTrue(html.contains("action=\"/language\""));
		org.junit.jupiter.api.Assertions.assertTrue(html.contains("value=\"pt-BR\""));
		org.junit.jupiter.api.Assertions.assertTrue(html.contains("value=\"en\""));
	}

	@Test
	void tutorAndVeterinarianFormsReachRegistrationService() throws Exception {
		for (String role : new String[] { "ROLE_CLIENTE", "ROLE_VETERINARIO" }) {
			AuthService auth = mock(AuthService.class);
			LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
			validator.afterPropertiesSet();
			var mvc = MockMvcBuilders.standaloneSetup(new PublicWebController(auth, mock(CurrentUserService.class)))
					.setValidator(validator).build();

			mvc.perform(post("/register")
					.param("name", "Ana Silva")
					.param("email", "ana@example.com")
					.param("password", "segredo123")
					.param("role", role))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login?registered"));
			verify(auth).register(argThat(request -> "Ana Silva".equals(request.name())
					&& "ana@example.com".equals(request.email()) && role.equals(request.role())));
		}
	}
}
