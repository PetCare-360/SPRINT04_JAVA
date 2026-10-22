package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.DeviceRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;
import br.com.fiap.petcare360_java.service.AlertPolicy;
import br.com.fiap.petcare360_java.service.ClinicalAccessService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.PetMapper;
import br.com.fiap.petcare360_java.service.PetService;

class ClinicalPermissionsTest {
	@Test
	void administratorCannotSendMessageOutsideTutorVeterinarianPair() {
		CurrentUserService current = mock(CurrentUserService.class);
		PetVeterinarianRepository links = mock(PetVeterinarianRepository.class);
		ClinicalAccessService access = new ClinicalAccessService(current, mock(PetRepository.class),
				mock(AppUserRepository.class), links);
		AppUser tutor = user(1L, "tutor@example.com");
		Pet pet = new Pet();
		pet.setId(10L);
		pet.setUser(tutor);
		when(current.user()).thenReturn(user(2L, "admin@example.com"));
		when(current.hasRole("ROLE_ADMIN")).thenReturn(true);

		assertThrows(ApiException.class, () -> access.validateConversation(pet, user(3L, "vet@example.com")));
	}

	@Test
	void assignedVeterinarianCannotEditTutorsPet() {
		CurrentUserService current = mock(CurrentUserService.class);
		PetRepository pets = mock(PetRepository.class);
		PetVeterinarianRepository links = mock(PetVeterinarianRepository.class);
		ClinicalAccessService access = mock(ClinicalAccessService.class);
		PetService service = new PetService(pets, mock(DeviceRepository.class), mock(SensorDataRepository.class),
				mock(AlertRepository.class), links, current, access, mock(AlertPolicy.class), new PetMapper());
		when(current.hasRole("ROLE_VETERINARIO")).thenReturn(true);
		when(current.email()).thenReturn("vet@example.com");
		when(links.existsActiveLink(10L, "vet@example.com")).thenReturn(true);

		assertThrows(ApiException.class, () -> service.findEditablePet(10L));
	}

	private AppUser user(long id, String email) {
		AppUser user = new AppUser();
		user.setId(id);
		user.setEmail(email);
		return user;
	}
}
