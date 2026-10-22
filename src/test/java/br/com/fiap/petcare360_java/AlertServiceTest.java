package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.dto.AlertStatusUpdateRequest;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AlertStatusEnum;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.AlertHistoryRepository;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.service.AlertService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.PetMapper;
import br.com.fiap.petcare360_java.service.PetService;
import jakarta.persistence.EntityManager;

class AlertServiceTest {

	private final AlertRepository alerts = mock(AlertRepository.class);
	private final AlertHistoryRepository history = mock(AlertHistoryRepository.class);
	private final PetService pets = mock(PetService.class);
	private final CurrentUserService currentUser = mock(CurrentUserService.class);
	private final OracleClinicalProcedures procedures = mock(OracleClinicalProcedures.class);
	private final EntityManager entityManager = mock(EntityManager.class);
	private final PetVeterinarianRepository links = mock(PetVeterinarianRepository.class);
	private final AlertService service = new AlertService(alerts, history, pets, currentUser, new PetMapper(), procedures, entityManager, links);
	private Alert alert;

	@BeforeEach
	void setUp() {
		var pet = new Pet();
		pet.setId(1L);
		var tutor = new AppUser();
		tutor.setId(1L);
		pet.setUser(tutor);
		alert = new Alert();
		alert.setId(10L);
		alert.setPet(pet);
		alert.setStatus(AlertStatusEnum.OPEN);
		when(alerts.findByIdAndPetId(10L, 1L)).thenReturn(Optional.of(alert));
		var actor = new AppUser();
		actor.setId(2L);
		actor.setName("Veterinario");
		actor.setEmail("vet@example.com");
		when(currentUser.user()).thenReturn(actor);
		when(currentUser.hasRole("ROLE_VETERINARIO")).thenReturn(true);
		when(currentUser.email()).thenReturn("vet@example.com");
		when(links.existsActiveLink(1L, "vet@example.com")).thenReturn(true);
	}

	@Test
	void delegatesValidTransitionAndAuditToOracleProcedure() {
		service.updateStatus(1L, 10L, new AlertStatusUpdateRequest(AlertStatusEnum.IN_PROGRESS, "Em análise"));
		verify(procedures).updateAlert(10L, 2L, "IN_PROGRESS", "Em análise");
		verify(entityManager).refresh(alert);
	}

	@Test
	void rejectsSkippingARequiredLifecycleStep() {
		assertThrows(ApiException.class, () -> service.updateStatus(1L, 10L,
				new AlertStatusUpdateRequest(AlertStatusEnum.RESOLVED, null)));
	}

	@Test
	void tutorCanManageAlertStatusAsOracleAllows() {
		var tutor = new AppUser();
		tutor.setId(1L);
		when(currentUser.user()).thenReturn(tutor);
		service.updateStatus(1L, 10L, new AlertStatusUpdateRequest(AlertStatusEnum.IN_PROGRESS, null));
		verify(procedures).updateAlert(10L, 1L, "IN_PROGRESS", null);
	}

	@Test
	void unrelatedAdminCannotBypassOracleProcedure() {
		when(links.existsActiveLink(1L, "vet@example.com")).thenReturn(false);
		assertThrows(ApiException.class, () -> service.updateStatus(1L, 10L,
				new AlertStatusUpdateRequest(AlertStatusEnum.IN_PROGRESS, null)));
	}
}
