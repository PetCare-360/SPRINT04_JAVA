package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.Device;
import br.com.fiap.petcare360_java.model.DeviceStatusEnum;
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

class VeterinarianPetEditTest {
    private final PetRepository pets = mock(PetRepository.class);
    private final PetVeterinarianRepository links = mock(PetVeterinarianRepository.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final PetService service = new PetService(pets, mock(DeviceRepository.class),
            mock(SensorDataRepository.class), mock(AlertRepository.class), links, current,
            mock(ClinicalAccessService.class), mock(AlertPolicy.class), new PetMapper());

    @Test
    void linkedVeterinarianCanEditAndDeactivatePet() {
        Pet pet = new Pet();
        pet.setId(10L);
        pet.setActive(true);
        Device device = new Device();
        device.setDeviceCode("DEV-001");
        pet.setDevice(device);
        when(current.hasRole("ROLE_VETERINARIO")).thenReturn(true);
        when(current.email()).thenReturn("vet@example.com");
        when(links.existsActiveLink(10L, "vet@example.com")).thenReturn(true);
        when(pets.findById(10L)).thenReturn(Optional.of(pet));
        when(pets.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateBasic(10L, "Thor", 5, new BigDecimal("12.50"), "Golden Retriever", "DOG", "DEV-001");
        assertEquals("Thor", pet.getName());
        service.deactivate(10L);
        assertEquals(false, pet.getActive());
        assertEquals(DeviceStatusEnum.INACTIVE, device.getStatus());
        verify(pets, org.mockito.Mockito.times(2)).save(pet);
    }

    @Test
    void unlinkedVeterinarianCannotEditPet() {
        when(current.hasRole("ROLE_VETERINARIO")).thenReturn(true);
        when(current.email()).thenReturn("other-vet@example.com");
        assertThrows(ApiException.class, () -> service.findEditablePet(10L));
        assertThrows(ApiException.class, () -> service.deactivate(10L));
    }
}
