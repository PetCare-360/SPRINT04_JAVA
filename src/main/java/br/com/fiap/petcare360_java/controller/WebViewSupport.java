package br.com.fiap.petcare360_java.controller;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import br.com.fiap.petcare360_java.dto.AppointmentResponse;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.MessageService;
import br.com.fiap.petcare360_java.service.PetService;
import br.com.fiap.petcare360_java.service.RecommendationService;

@Component
class WebViewSupport {

	private final PetService petService;
	private final MessageService messageService;
	private final AppointmentService appointmentService;
	private final RecommendationService recommendationService;
	private final CurrentUserService currentUserService;
	private final AppUserRepository userRepository;
	private final PetRepository petRepository;
	private final PetVeterinarianRepository linkRepository;
	private final AlertRepository alertRepository;

	WebViewSupport(PetService petService, MessageService messageService, AppointmentService appointmentService,
			RecommendationService recommendationService, CurrentUserService currentUserService,
			AppUserRepository userRepository, PetRepository petRepository,
			PetVeterinarianRepository linkRepository, AlertRepository alertRepository) {
		this.petService = petService;
		this.messageService = messageService;
		this.appointmentService = appointmentService;
		this.recommendationService = recommendationService;
		this.currentUserService = currentUserService;
		this.userRepository = userRepository;
		this.petRepository = petRepository;
		this.linkRepository = linkRepository;
		this.alertRepository = alertRepository;
	}

	void addTutorData(Model model) {
		var pets = petService.listAll();
		var links = linkRepository.findActiveLinksByTutorEmail(currentUserService.email());
		var messages = messageService.listMine();
		var appointments = appointmentService.listMine();
		var recommendations = recommendationService.listMine();
		model.addAttribute("pets", pets);
		model.addAttribute("veterinarians", userRepository.findByRolesName("ROLE_VETERINARIO"));
		model.addAttribute("linkedVeterinarians", links);
		addCommunicationData(model, messages, appointments, recommendations);
		model.addAttribute("petCount", pets.size());
		model.addAttribute("linkCount", links.size());
		model.addAttribute("alertCount", pets.stream().mapToLong(pet -> alertRepository.countByPetId(pet.id())).sum());
	}

	void addVetData(Model model) {
		var links = linkRepository.findActiveLinksByVeterinarianEmail(currentUserService.email());
		var messages = messageService.listMine();
		var appointments = appointmentService.listMine();
		var recommendations = recommendationService.listMine();
		model.addAttribute("linkedPets", links);
		addCommunicationData(model, messages, appointments, recommendations);
		model.addAttribute("linkedPetCount", links.size());
		model.addAttribute("alertCount", links.stream()
				.mapToLong(link -> alertRepository.countByPetId(link.getPet().getId())).sum());
	}

	void addAdminData(Model model) {
		var users = userRepository.findAll();
		var pets = petRepository.findAll();
		var links = linkRepository.findAll();
		var messages = messageService.listAll();
		var appointments = appointmentService.listAll();
		var recommendations = recommendationService.listAll();
		model.addAttribute("users", users);
		model.addAttribute("pets", pets);
		model.addAttribute("links", links);
		model.addAttribute("veterinarians", userRepository.findByRolesName("ROLE_VETERINARIO"));
		addCommunicationData(model, messages, appointments, recommendations);
		model.addAttribute("userCount", users.size());
		model.addAttribute("petCount", pets.size());
		model.addAttribute("activePetCount", pets.stream().filter(pet -> Boolean.TRUE.equals(pet.getActive())).count());
		model.addAttribute("linkCount", links.size());
	}

	@SuppressWarnings("unchecked")
	void applyAppointmentFilter(String status, Model model) {
		if (status == null || status.isBlank()) return;
		var appointments = (List<AppointmentResponse>) model.getAttribute("appointments");
		if (appointments != null) {
			model.addAttribute("appointments", appointments.stream()
					.filter(item -> item.status().name().equals(status)).toList());
		}
	}

	private void addCommunicationData(Model model, List<?> messages, List<?> appointments, List<?> recommendations) {
		model.addAttribute("messages", messages);
		model.addAttribute("appointments", appointments);
		model.addAttribute("recommendations", recommendations);
		model.addAttribute("messageCount", messages.size());
		model.addAttribute("appointmentCount", appointments.size());
		model.addAttribute("recommendationCount", recommendations.size());
	}
}
