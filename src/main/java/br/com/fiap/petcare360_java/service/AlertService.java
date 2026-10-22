package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.AlertHistoryResponse;
import br.com.fiap.petcare360_java.dto.AlertResponse;
import br.com.fiap.petcare360_java.dto.AlertStatusUpdateRequest;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AlertStatusEnum;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.repository.AlertHistoryRepository;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import jakarta.persistence.EntityManager;

@Service
public class AlertService {

	private final AlertRepository alertRepository;
	private final AlertHistoryRepository historyRepository;
	private final PetService petService;
	private final CurrentUserService currentUserService;
	private final PetMapper mapper;
	private final OracleClinicalProcedures oracleClinicalProcedures;
	private final EntityManager entityManager;
	private final PetVeterinarianRepository veterinarianRepository;

	public AlertService(AlertRepository alertRepository, AlertHistoryRepository historyRepository,
			PetService petService, CurrentUserService currentUserService, PetMapper mapper,
			OracleClinicalProcedures oracleClinicalProcedures, EntityManager entityManager,
			PetVeterinarianRepository veterinarianRepository) {
		this.alertRepository = alertRepository;
		this.historyRepository = historyRepository;
		this.petService = petService;
		this.currentUserService = currentUserService;
		this.mapper = mapper;
		this.oracleClinicalProcedures = oracleClinicalProcedures;
		this.entityManager = entityManager;
		this.veterinarianRepository = veterinarianRepository;
	}

	@Transactional
	@CacheEvict(value = { "petAlerts", "petSummary", "petQuickAlerts" }, allEntries = true)
	public AlertResponse updateStatus(Long petId, Long alertId, AlertStatusUpdateRequest request) {
		petService.findAccessiblePet(petId);

		Alert alert = alertRepository.findByIdAndPetId(alertId, petId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Alerta não encontrado"));
		AppUser actor = currentUserService.user();
		assertCanManageAlerts(alert, actor);
		AlertStatusEnum previous = alert.getStatus();
		AlertStatusEnum target = request.status();

		if (previous == target) {
			return mapper.toAlertResponse(alert);
		}
		if (!isValidTransition(previous, target)) {
			throw new ApiException(HttpStatus.CONFLICT,
					"Transição de alerta inválida: " + previous + " -> " + target);
		}

		oracleClinicalProcedures.updateAlert(alertId, actor.getId(), target.name(), cleanNote(request.note()));
		entityManager.refresh(alert);
		return mapper.toAlertResponse(alert);
	}

	@Transactional(readOnly = true)
	public List<AlertHistoryResponse> history(Long petId, Long alertId) {
		petService.findAccessiblePet(petId);
		alertRepository.findByIdAndPetId(alertId, petId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Alerta não encontrado"));

		return historyRepository.findByAlertIdOrderByCreatedAtAsc(alertId).stream()
				.map(item -> new AlertHistoryResponse(
						item.getId(), item.getPreviousStatus(), item.getNewStatus(), item.getNote(),
						item.getUser().getId(), item.getUser().getName(), item.getCreatedAt()))
				.toList();
	}

	private void assertCanManageAlerts(Alert alert, AppUser actor) {
		boolean isTutor = alert.getPet().getUser().getId().equals(actor.getId());
		boolean isAssignedVet = veterinarianRepository.existsActiveLink(alert.getPet().getId(), actor.getEmail());
		if (!isTutor && !isAssignedVet) {
			throw new ApiException(HttpStatus.FORBIDDEN,
					"Apenas o tutor ou veterinário vinculado pode alterar alertas");
		}
	}

	private boolean isValidTransition(AlertStatusEnum current, AlertStatusEnum target) {
		return current == AlertStatusEnum.OPEN && target == AlertStatusEnum.IN_PROGRESS
				|| current == AlertStatusEnum.IN_PROGRESS && target == AlertStatusEnum.RESOLVED;
	}

	private String cleanNote(String note) {
		return note == null || note.isBlank() ? null : note.trim();
	}
}
