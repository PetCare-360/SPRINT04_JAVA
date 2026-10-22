package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.dto.MessageRequest;
import br.com.fiap.petcare360_java.dto.RecommendationRequest;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.RecommendationPriorityEnum;
import br.com.fiap.petcare360_java.repository.AppointmentRepository;
import br.com.fiap.petcare360_java.repository.CareRecommendationRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.repository.PetMessageRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.ClinicalAccessService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.MessageService;
import br.com.fiap.petcare360_java.service.RecommendationService;

class ClinicalProcedureServicesTest {
	private final CurrentUserService currentUser = mock(CurrentUserService.class);
	private final ClinicalAccessService access = mock(ClinicalAccessService.class);
	private final PetVeterinarianRepository links = mock(PetVeterinarianRepository.class);
	private final OracleClinicalProcedures procedures = mock(OracleClinicalProcedures.class);

	private AppUser user(long id, String email) {
		AppUser user = new AppUser();
		user.setId(id);
		user.setEmail(email);
		return user;
	}

	private Pet pet(AppUser tutor) {
		Pet pet = new Pet();
		pet.setId(10L);
		pet.setUser(tutor);
		when(access.accessiblePet(10L)).thenReturn(pet);
		return pet;
	}

	@Test
	void messageUsesOracleProcedureAndDoesNotSaveTwice() {
		AppUser tutor = user(1L, "tutor@example.com");
		AppUser vet = user(2L, "vet@example.com");
		pet(tutor);
		when(access.userById(2L)).thenReturn(vet);
		when(currentUser.user()).thenReturn(tutor);
		PetMessageRepository repository = mock(PetMessageRepository.class);
		MessageService service = new MessageService(repository, currentUser, access,
				mock(ApplicationEventPublisher.class), procedures);

		service.send(new MessageRequest(10L, 2L, " Assunto ", " Texto "));

		verify(procedures).sendMessage(10L, 1L, 2L, "Assunto", "Texto");
		verify(repository, never()).save(any());
	}

	@Test
	void appointmentUsesOracleProcedure() {
		AppUser tutor = user(1L, "tutor@example.com");
		AppUser vet = user(2L, "vet@example.com");
		pet(tutor);
		when(currentUser.user()).thenReturn(tutor);
		when(access.userById(2L)).thenReturn(vet);
		when(links.existsActiveLink(10L, "vet@example.com")).thenReturn(true);
		AppointmentRepository repository = mock(AppointmentRepository.class);
		AppointmentService service = new AppointmentService(repository, currentUser, access, links, procedures);
		OffsetDateTime date = OffsetDateTime.now().plusDays(2);

		service.request(new AppointmentRequest(10L, 2L, date, " Consulta "));

		verify(procedures).requestAppointment(10L, 1L, 2L, date, "Consulta");
		verify(repository, never()).save(any());
	}

	@Test
	void recommendationUsesOracleProcedureOnlyForAssignedVeterinarian() {
		AppUser tutor = user(1L, "tutor@example.com");
		AppUser vet = user(2L, "vet@example.com");
		pet(tutor);
		when(currentUser.user()).thenReturn(vet);
		when(currentUser.hasRole("ROLE_VETERINARIO")).thenReturn(true);
		when(links.existsActiveLink(10L, "vet@example.com")).thenReturn(true);
		CareRecommendationRepository repository = mock(CareRecommendationRepository.class);
		RecommendationService service = new RecommendationService(repository, currentUser, access, links, procedures);
		RecommendationRequest request = new RecommendationRequest(10L, " Título ", " Orientações ",
				RecommendationPriorityEnum.HIGH);

		service.create(request);

		verify(procedures).createRecommendation(10L, 1L, 2L, "Título", "Orientações", "HIGH");
		verify(repository, never()).save(any());
	}

	@Test
	void administratorCannotCreateRecommendationOutsideOracleRule() {
		RecommendationService service = new RecommendationService(mock(CareRecommendationRepository.class),
				currentUser, access, links, procedures);
		when(currentUser.hasRole("ROLE_ADMIN")).thenReturn(true);

		assertThrows(ApiException.class, () -> service.create(new RecommendationRequest(10L,
				"Título", "Orientações", RecommendationPriorityEnum.NORMAL)));
		verify(procedures, never()).createRecommendation(any(Long.class), any(Long.class), any(Long.class),
				any(String.class), any(String.class), any(String.class));
	}
}
