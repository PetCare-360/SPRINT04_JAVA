package br.com.fiap.petcare360_java.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.ClinicalPageService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.HealthTrendService;
import br.com.fiap.petcare360_java.service.PetService;

@Controller
public class ClinicalWebController {
	private static final DateTimeFormatter HTML_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	private final PetService petService;
	private final AppointmentService appointmentService;
	private final CurrentUserService currentUserService;
	private final ClinicalPageService clinicalPageService;
	private final HealthTrendService healthTrends;

	public ClinicalWebController(PetService petService, AppointmentService appointmentService,
			CurrentUserService currentUserService, ClinicalPageService clinicalPageService,
			HealthTrendService healthTrends) {
		this.petService = petService;
		this.appointmentService = appointmentService;
		this.currentUserService = currentUserService;
		this.clinicalPageService = clinicalPageService;
		this.healthTrends = healthTrends;
	}

	@GetMapping({ "/tutor/pets/{id}", "/vet/pets/{id}", "/admin/pets/{id}" })
	public String petDetail(@PathVariable Long id, Model model) {
		var detail = clinicalPageService.detail(id);
		model.addAttribute("pet", detail.pet());
		model.addAttribute("latestReading", detail.latestReading());
		model.addAttribute("readings", detail.readings());
		model.addAttribute("healthTrend", healthTrends.from(detail.readings(), detail.alerts()));
		model.addAttribute("alerts", detail.alerts());
		model.addAttribute("links", detail.links());
		model.addAttribute("canEditPet", currentUserService.hasRole("ROLE_ADMIN")
				|| (currentUserService.hasRole("ROLE_VETERINARIO")
						&& petService.canCurrentVetEdit(id))
				|| detail.pet().getUser().getId().equals(currentUserService.user().getId()));
		addRoleFlags(model);
		return "pet-detail";
	}

	@GetMapping({ "/tutor/pets/{id}/editar", "/vet/pets/{id}/editar", "/admin/pets/{id}/editar" })
	public String editPet(@PathVariable Long id, Model model) {
		model.addAttribute("pet", petService.findEditablePet(id));
		addRoleFlags(model);
		return "pet-edit";
	}

	@GetMapping({ "/tutor/consultas/{id}", "/vet/consultas/{id}", "/admin/consultas/{id}" })
	public String appointmentDetail(@PathVariable Long id, Model model) {
		var appointment = appointmentService.findEditableAppointment(id);
		var petDetail = clinicalPageService.detail(appointment.getPet().getId());
		model.addAttribute("appointment", appointment);
		model.addAttribute("consultationPet", petDetail.pet());
		model.addAttribute("consultationReading", petDetail.latestReading());
		model.addAttribute("consultationAlerts", petDetail.alerts().stream().limit(5).toList());
		model.addAttribute("canEditPet", currentUserService.hasRole("ROLE_ADMIN")
				|| (currentUserService.hasRole("ROLE_VETERINARIO")
						&& petService.canCurrentVetEdit(appointment.getPet().getId()))
				|| appointment.getPet().getUser().getId().equals(currentUserService.user().getId()));
		addRoleFlags(model);
		return "appointment-detail";
	}

	@GetMapping({ "/tutor/consultas/{id}/editar", "/vet/consultas/{id}/editar", "/admin/consultas/{id}/editar" })
	public String editAppointment(@PathVariable Long id, Model model) {
		var appointment = appointmentService.findEditableAppointment(id);
		model.addAttribute("appointment", appointment);
		model.addAttribute("scheduledAtValue", appointment.getScheduledAt().toLocalDateTime().format(HTML_DATETIME));
		model.addAttribute("linkedVeterinarians", clinicalPageService.appointmentOptions());
		addRoleFlags(model);
		return "appointment-edit";
	}

	private void addRoleFlags(Model model) {
		model.addAttribute("isAdmin", currentUserService.hasRole("ROLE_ADMIN"));
		model.addAttribute("isVet", currentUserService.hasRole("ROLE_VETERINARIO"));
		model.addAttribute("isTutor", currentUserService.hasRole("ROLE_CLIENTE"));
	}

}
