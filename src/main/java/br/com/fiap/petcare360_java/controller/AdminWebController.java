package br.com.fiap.petcare360_java.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.fiap.petcare360_java.service.AdminDirectoryService;

@Controller
public class AdminWebController {
	private final WebViewSupport views;
	private final AdminDirectoryService directory;

	public AdminWebController(WebViewSupport views, AdminDirectoryService directory) {
		this.views = views; this.directory = directory;
	}

	@GetMapping("/admin")
	public String dashboard(Model model) { views.addAdminData(model); return "admin/index"; }

	@GetMapping("/admin/usuarios")
	public String users(@RequestParam(required = false) String role, Model model) {
		views.addAdminData(model);
		model.addAttribute("users", directory.users(role));
		model.addAttribute("selectedRole", role); return "admin/usuarios";
	}

	@GetMapping("/admin/pets")
	public String pets(@RequestParam(required = false, defaultValue = "todos") String active, Model model) {
		views.addAdminData(model);
		model.addAttribute("pets", directory.pets(active));
		model.addAttribute("selectedActive", active); return "admin/pets";
	}

	@GetMapping("/admin/vinculos")
	public String links(Model model) { views.addAdminData(model); return "admin/vinculos"; }

	@GetMapping("/admin/mensagens")
	public String messages(Model model) { views.addAdminData(model); return "admin/mensagens"; }

	@GetMapping("/admin/consultas")
	public String appointments(@RequestParam(required = false) String status, Model model) {
		views.addAdminData(model); views.applyAppointmentFilter(status, model);
		model.addAttribute("selectedStatus", status); return "admin/consultas";
	}

	@GetMapping("/admin/recomendacoes")
	public String recommendations(Model model) { views.addAdminData(model); return "admin/recomendacoes"; }
}
