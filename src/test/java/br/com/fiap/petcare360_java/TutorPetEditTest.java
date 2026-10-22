package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Device;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.DeviceRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;
import br.com.fiap.petcare360_java.service.AlertPolicy;
import br.com.fiap.petcare360_java.service.ClinicalAccessService;
import br.com.fiap.petcare360_java.service.CurrentUserService;
import br.com.fiap.petcare360_java.service.PetMapper;
import br.com.fiap.petcare360_java.service.PetService;

class TutorPetEditTest {
	@Test
	void tutorCanUpdateOwnPet() {
		PetRepository pets = mock(PetRepository.class);
		SensorDataRepository readings = mock(SensorDataRepository.class);
		CurrentUserService current = mock(CurrentUserService.class);
		PetService service = new PetService(pets, mock(DeviceRepository.class), readings,
				mock(AlertRepository.class), mock(PetVeterinarianRepository.class), current,
				mock(ClinicalAccessService.class), mock(AlertPolicy.class), new PetMapper());
		AppUser tutor = new AppUser();
		tutor.setId(1L);
		Pet pet = new Pet();
		pet.setId(10L);
		pet.setUser(tutor);
		Device device = new Device();
		device.setDeviceCode("DEV-001");
		pet.setDevice(device);
		when(current.email()).thenReturn("tutor@example.com");
		when(pets.findByIdAndUserEmail(10L, "tutor@example.com")).thenReturn(Optional.of(pet));
		when(pets.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(readings.findFirstByDevicePetIdOrderByTimestampDesc(10L)).thenReturn(Optional.empty());

		var result = service.updateBasic(10L, "Thor", 6, new BigDecimal("13.20"),
				"Golden Retriever", "DOG", "DEV-001");

		assertEquals("Thor", result.name());
		assertEquals(6, result.age());
		assertEquals(new BigDecimal("13.20"), result.weight());
		verify(pets).save(pet);
	}
}
