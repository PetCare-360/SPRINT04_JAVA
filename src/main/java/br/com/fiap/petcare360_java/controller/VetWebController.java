package br.com.fiap.petcare360_java.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VetWebController {
	private final WebViewSupport views;

	public VetWebController(WebViewSupport views) { this.views = views; }

	@GetMapping("/vet")
	public String dashboard(Model model) { views.addVetData(model); return "vet/index"; }

	@GetMapping("/vet/pets")
	public String pets(Model model) { views.addVetData(model); return "vet/pets"; }

	@GetMapping("/vet/mensagens")
	public String messages(Model model) { views.addVetData(model); return "vet/mensagens"; }

	@GetMapping("/vet/consultas")
	public String appointments(@RequestParam(required = false) String status, Model model) {
		views.addVetData(model); views.applyAppointmentFilter(status, model);
		model.addAttribute("selectedStatus", status); return "vet/consultas";
	}

	@GetMapping("/vet/recomendacoes")
	public String recommendations(Model model) { views.addVetData(model); return "vet/recomendacoes"; }
}
