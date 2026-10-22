package br.com.fiap.petcare360_java.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TutorWebController {
	private final WebViewSupport views;

	public TutorWebController(WebViewSupport views) { this.views = views; }

	@GetMapping("/tutor")
	public String dashboard(Model model) { views.addTutorData(model); return "tutor/index"; }

	@GetMapping("/tutor/pets")
	public String pets(Model model) { views.addTutorData(model); return "tutor/pets"; }

	@GetMapping("/tutor/pets/novo")
	public String newPet() { return "tutor/pet-form"; }

	@GetMapping("/tutor/veterinarios")
	public String veterinarians(Model model) { views.addTutorData(model); return "tutor/veterinarios"; }

	@GetMapping("/tutor/mensagens")
	public String messages(@RequestParam(defaultValue = "0") int page, Model model) {
		views.addTutorData(model); views.addMessagesPage(model, page, "tutor"); return "tutor/mensagens";
	}

	@GetMapping("/tutor/consultas")
	public String appointments(@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page, Model model) {
		views.addTutorData(model); views.addAppointmentsPage(model, page, "tutor", status);
		return "tutor/consultas";
	}

	@GetMapping("/tutor/recomendacoes")
	public String recommendations(@RequestParam(defaultValue = "0") int page, Model model) {
		views.addTutorData(model); views.addRecommendationsPage(model, page, "tutor"); return "tutor/recomendacoes";
	}
}
