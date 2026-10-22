package br.com.fiap.petcare360_java.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.fiap.petcare360_java.dto.RegisterRequest;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.service.AuthService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import jakarta.validation.Valid;

@Controller
public class PublicWebController {
	private final AuthService authService;
	private final CurrentUserService currentUserService;

	public PublicWebController(AuthService authService, CurrentUserService currentUserService) {
		this.authService = authService;
		this.currentUserService = currentUserService;
	}

	@GetMapping("/")
	public String home() { return "redirect:/dashboard"; }

	@GetMapping("/login")
	public String login() { return "login"; }

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("registerRequest", new RegisterRequest(null, null, null, "ROLE_CLIENTE"));
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest request, BindingResult errors) {
		if (errors.hasErrors()) return "register";
		try {
			authService.register(request);
		} catch (ApiException ex) {
			errors.reject("registration", ex.getMessage());
			return "register";
		}
		return "redirect:/login?registered";
	}

	@GetMapping("/dashboard")
	public String dashboard() {
		if (currentUserService.hasRole("ROLE_ADMIN")) return "redirect:/admin";
		if (currentUserService.hasRole("ROLE_VETERINARIO")) return "redirect:/vet";
		return "redirect:/tutor";
	}
}
