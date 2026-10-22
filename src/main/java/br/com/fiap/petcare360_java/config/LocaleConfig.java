package br.com.fiap.petcare360_java.config;

import java.util.Locale;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public class LocaleConfig {
	@Bean
	LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver("PETCARE_LANGUAGE");
		resolver.setCookiePath("/");
		resolver.setCookieHttpOnly(true);
		resolver.setCookieSameSite("Lax");
		resolver.setCookieMaxAge(Duration.ofDays(365));
		resolver.setDefaultLocaleFunction(request ->
				"en".equals(request.getLocale().getLanguage()) ? Locale.ENGLISH : Locale.forLanguageTag("pt-BR"));
		return resolver;
	}
}
