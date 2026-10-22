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
	public String messages(Model model) { views.addTutorData(model); return "tutor/mensagens"; }

	@GetMapping("/tutor/consultas")
	public String appointments(@RequestParam(required = false) String status, Model model) {
		views.addTutorData(model); views.applyAppointmentFilter(status, model);
		model.addAttribute("selectedStatus", status); return "tutor/consultas";
	}

	@GetMapping("/tutor/recomendacoes")
	public String recommendations(Model model) { views.addTutorData(model); return "tutor/recomendacoes"; }
}
