package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import br.com.fiap.petcare360_java.controller.LanguageController;

class LanguageControllerTest {
	@Test
	void selectionPersistsInCookieAndReturnsToCurrentPage() throws Exception {
		CookieLocaleResolver resolver = new CookieLocaleResolver("PETCARE_LANGUAGE");
		var mvc = MockMvcBuilders.standaloneSetup(new LanguageController(resolver), new LocalePage())
				.setLocaleResolver(resolver).build();

		var response = mvc.perform(post("/language").param("lang", "en")
				.header("Referer", "http://localhost/sample"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/sample"))
				.andReturn().getResponse();
		var cookie = response.getCookie("PETCARE_LANGUAGE");
		assertNotNull(cookie);
		mvc.perform(get("/sample").cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(content().string("en"));
	}

	@Controller
	static class LocalePage {
		@GetMapping("/sample")
		@ResponseBody
		String sample(Locale locale) {
			return locale.toLanguageTag();
		}
	}
}
