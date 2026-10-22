package br.com.fiap.petcare360_java.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.dto.InitialSensorDataRequest;
import br.com.fiap.petcare360_java.dto.MessageRequest;
import br.com.fiap.petcare360_java.dto.PetRequest;
import br.com.fiap.petcare360_java.dto.PetBasicRequest;
import br.com.fiap.petcare360_java.dto.RecommendationRequest;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkRequest;
import br.com.fiap.petcare360_java.model.RecommendationPriorityEnum;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.MessageService;
import br.com.fiap.petcare360_java.service.PetService;
import br.com.fiap.petcare360_java.service.RecommendationService;
import br.com.fiap.petcare360_java.service.VeterinarianLinkService;
import jakarta.validation.Validator;

@Controller
public class WebFormController {
	private final PetService petService;
	private final MessageService messageService;
	private final AppointmentService appointmentService;
	private final RecommendationService recommendationService;
	private final VeterinarianLinkService linkService;
	private final CurrentUserService currentUserService;
	private final Validator validator;

	public WebFormController(PetService petService, MessageService messageService,
			AppointmentService appointmentService, RecommendationService recommendationService,
			VeterinarianLinkService linkService, CurrentUserService currentUserService, Validator validator) {
		this.petService = petService;
		this.messageService = messageService;
		this.appointmentService = appointmentService;
		this.recommendationService = recommendationService;
		this.linkService = linkService;
		this.currentUserService = currentUserService;
		this.validator = validator;
	}

	@PostMapping("/messages/form")
	public String sendMessage(@RequestParam(required = false) Long petId,
			@RequestParam(required = false) Long receiverId,
			@RequestParam(required = false) String conversationKey,
			@RequestParam String subject, @RequestParam String message) {
		Long[] selected = selectedPair(conversationKey, petId, receiverId);
		messageService.send(checked(new MessageRequest(selected[0], selected[1], subject, message)));
		return redirectToArea("mensagens");
	}

	@PostMapping("/pets/form")
	public String createPet(@RequestParam String name, @RequestParam Integer age,
			@RequestParam BigDecimal weight, @RequestParam String breed, @RequestParam String species,
			@RequestParam String deviceId, @RequestParam BigDecimal temperature,
			@RequestParam Integer heartRate, @RequestParam Integer activityLevel, @RequestParam Integer battery,
			@RequestParam BigDecimal latitude, @RequestParam BigDecimal longitude) {
		var sensor = new InitialSensorDataRequest(OffsetDateTime.now(), temperature, heartRate,
				activityLevel, latitude, longitude, battery);
		petService.create(checked(new PetRequest(name, age, weight, breed, species, deviceId, sensor)));
		return "redirect:/tutor/pets";
	}

	@PostMapping("/pets/{id}/form")
	public String updatePet(@PathVariable Long id, @RequestParam String name, @RequestParam Integer age,
			@RequestParam BigDecimal weight, @RequestParam String breed, @RequestParam String species,
			@RequestParam String deviceId) {
		var request = checked(new PetBasicRequest(name, age, weight, breed, species, deviceId));
		petService.updateBasic(id, request.name(), request.age(), request.weight(),
				request.breed(), request.species(), request.deviceId());
		return redirectToArea("pets");
	}

	@PostMapping("/pets/{id}/remover")
	public String removePet(@PathVariable Long id) {
		petService.deactivate(id);
		return redirectToArea("pets");
	}

	@PostMapping("/appointments/form")
	public String requestAppointment(@RequestParam(required = false) Long petId,
			@RequestParam(required = false) Long veterinarianId,
			@RequestParam(required = false) String appointmentKey,
			@RequestParam String scheduledAt, @RequestParam String reason, RedirectAttributes redirect) {
		try {
			Long[] selected = selectedPair(appointmentKey, petId, veterinarianId);
			appointmentService.request(checked(new AppointmentRequest(selected[0], selected[1], toOffsetDateTime(scheduledAt), reason)));
			return redirectToArea("consultas");
		} catch (ApiException ex) {
			redirect.addFlashAttribute("appointmentError", ex.getMessage());
			return redirectToArea("consultas");
		}
	}

	@PostMapping("/appointments/{id}/form")
	public String updateAppointment(@PathVariable Long id, @RequestParam(required = false) Long petId,
			@RequestParam(required = false) Long veterinarianId,
			@RequestParam(required = false) String appointmentKey,
			@RequestParam String scheduledAt, @RequestParam String reason, RedirectAttributes redirect) {
		try {
			Long[] selected = selectedPair(appointmentKey, petId, veterinarianId);
			appointmentService.update(id, checked(new AppointmentRequest(selected[0], selected[1], toOffsetDateTime(scheduledAt), reason)));
			return redirectToArea("consultas");
		} catch (ApiException ex) {
			redirect.addFlashAttribute("appointmentError", ex.getMessage());
			return redirectToArea("consultas") + "/" + id + "/editar";
		}
	}

	@PostMapping("/appointments/{id}/finalizar")
	public String finishAppointment(@PathVariable Long id) {
		appointmentService.finish(id); return redirectToArea("consultas");
	}

	@PostMapping("/appointments/{id}/confirmar")
	public String confirmAppointment(@PathVariable Long id) {
		appointmentService.confirm(id); return redirectToArea("consultas");
	}

	@PostMapping("/appointments/{id}/cancelar")
	public String cancelAppointment(@PathVariable Long id) {
		appointmentService.cancel(id); return redirectToArea("consultas");
	}

	@PostMapping("/appointments/{id}/remover")
	public String removeAppointment(@PathVariable Long id) {
		appointmentService.cancel(id); return redirectToArea("consultas");
	}

	@PostMapping("/veterinarians/link/form")
	public String linkVeterinarian(@RequestParam Long petId, @RequestParam Long veterinarianId) {
		linkService.link(checked(new VeterinarianLinkRequest(petId, veterinarianId)));
		return redirectToArea("veterinarios");
	}

	@PostMapping("/recommendations/form")
	public String createRecommendation(@RequestParam Long petId, @RequestParam String title,
			@RequestParam String instructions, @RequestParam RecommendationPriorityEnum priority) {
		recommendationService.create(checked(new RecommendationRequest(petId, title, instructions, priority)));
		return redirectToArea("recomendacoes");
	}

	private OffsetDateTime toOffsetDateTime(String value) {
		try {
			return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toOffsetDateTime();
		} catch (DateTimeParseException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Data da consulta inválida");
		}
	}

	private String redirectToArea(String area) {
		if (currentUserService.hasRole("ROLE_ADMIN")) {
			return "redirect:/admin/" + ("veterinarios".equals(area) ? "vinculos" : area);
		}
		return currentUserService.hasRole("ROLE_VETERINARIO")
				? "redirect:/vet/" + area : "redirect:/tutor/" + area;
	}

	private Long[] selectedPair(String pairKey, Long firstId, Long secondId) {
		if (pairKey != null && !pairKey.isBlank()) {
			String[] ids = pairKey.split(":");
			if (ids.length != 2) throw new ApiException(HttpStatus.BAD_REQUEST, "Seleção inválida");
			try {
				return new Long[] { Long.valueOf(ids[0]), Long.valueOf(ids[1]) };
			} catch (NumberFormatException ex) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Seleção inválida");
			}
		}
		return new Long[] { firstId, secondId };
	}

	private <T> T checked(T request) {
		var errors = validator.validate(request);
		if (!errors.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, errors.iterator().next().getMessage());
		}
		return request;
	}
}
