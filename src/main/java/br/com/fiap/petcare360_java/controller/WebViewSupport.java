package br.com.fiap.petcare360_java.controller;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppointmentStatusEnum;
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
	private static final int PAGE_SIZE = 10;

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
		model.addAttribute("pets", pets);
		model.addAttribute("veterinarians", userRepository.findByRolesName("ROLE_VETERINARIO"));
		model.addAttribute("linkedVeterinarians", links);
		addCommunicationCounts(model, false);
		model.addAttribute("petCount", pets.size());
		model.addAttribute("linkCount", links.size());
		model.addAttribute("alertCount", pets.stream().mapToLong(pet -> alertRepository.countByPetId(pet.id())).sum());
	}

	void addVetData(Model model) {
		var links = linkRepository.findActiveLinksByVeterinarianEmail(currentUserService.email());
		model.addAttribute("linkedPets", links);
		addCommunicationCounts(model, false);
		model.addAttribute("linkedPetCount", links.size());
		model.addAttribute("alertCount", links.stream()
				.mapToLong(link -> alertRepository.countByPetId(link.getPet().getId())).sum());
	}

	void addAdminData(Model model) {
		var users = userRepository.findAll();
		var pets = petRepository.findAll();
		var links = linkRepository.findAll();
		model.addAttribute("users", users);
		model.addAttribute("pets", pets);
		model.addAttribute("links", links);
		model.addAttribute("veterinarians", userRepository.findByRolesName("ROLE_VETERINARIO"));
		addCommunicationCounts(model, true);
		model.addAttribute("userCount", users.size());
		model.addAttribute("petCount", pets.size());
		model.addAttribute("activePetCount", pets.stream().filter(pet -> Boolean.TRUE.equals(pet.getActive())).count());
		model.addAttribute("linkCount", links.size());
	}

	void addMessagesPage(Model model, int page, String area) {
		Page<?> result = fetchPage(page, pageable -> area.equals("admin")
				? messageService.pageAll(pageable) : messageService.pageMine(pageable));
		addPage(model, result, "messages", "/" + area + "/mensagens");
	}

	void addAppointmentsPage(Model model, int page, String area, String statusText) {
		AppointmentStatusEnum status = parseStatus(statusText);
		Page<?> result = fetchPage(page, pageable -> area.equals("admin")
				? appointmentService.pageAll(pageable, status) : appointmentService.pageMine(pageable, status));
		addPage(model, result, "appointments", "/" + area + "/consultas");
		model.addAttribute("selectedStatus", status == null ? "" : status.name());
	}

	void addRecommendationsPage(Model model, int page, String area) {
		Page<?> result = fetchPage(page, pageable -> area.equals("admin")
				? recommendationService.pageAll(pageable) : recommendationService.pageMine(pageable));
		addPage(model, result, "recommendations", "/" + area + "/recomendacoes");
	}

	private AppointmentStatusEnum parseStatus(String text) {
		if (text == null || text.isBlank()) return null;
		try {
			return AppointmentStatusEnum.valueOf(text);
		} catch (IllegalArgumentException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Status de consulta inválido");
		}
	}

	private <T> Page<T> fetchPage(int requestedPage, Function<Pageable, Page<T>> fetch) {
		Page<T> result = fetch.apply(PageRequest.of(Math.max(0, Math.min(requestedPage, 10_000)), PAGE_SIZE));
		if (result.getTotalPages() > 0 && result.getNumber() >= result.getTotalPages()) {
			return fetch.apply(PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE));
		}
		return result;
	}

	private void addPage(Model model, Page<?> result, String collection, String path) {
		model.addAttribute(collection, result.getContent());
		model.addAttribute("pageData", result);
		model.addAttribute("pageBasePath", path);
	}

	private void addCommunicationCounts(Model model, boolean admin) {
		model.addAttribute("messageCount", admin ? messageService.countAll() : messageService.countMine());
		model.addAttribute("appointmentCount", admin ? appointmentService.countAll() : appointmentService.countMine());
		model.addAttribute("recommendationCount", admin ? recommendationService.countAll() : recommendationService.countMine());
	}
}
