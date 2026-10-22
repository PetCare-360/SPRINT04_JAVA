package br.com.fiap.petcare360_java.controller;

import java.net.URI;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LanguageController {
	private final LocaleResolver localeResolver;

	public LanguageController(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	@PostMapping("/language")
	public String change(@RequestParam String lang, HttpServletRequest request, HttpServletResponse response) {
		Locale locale = switch (lang) {
			case "pt-BR" -> Locale.forLanguageTag("pt-BR");
			case "en" -> Locale.ENGLISH;
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idioma inválido");
		};
		localeResolver.setLocale(request, response, locale);

		String referer = request.getHeader("Referer");
		if (referer != null) {
			try {
				URI page = URI.create(referer);
				String path = page.getRawPath();
				if (request.getServerName().equalsIgnoreCase(page.getHost())
						&& path != null && path.startsWith("/") && !path.startsWith("//")
						&& !path.equals("/language")) {
					return "redirect:" + path;
				}
			} catch (IllegalArgumentException ignored) {
				// Sem página de origem válida: volta para a entrada da aplicação.
			}
		}
		return "redirect:/";
	}
}
