package br.com.fiap.petcare360_java;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Appointment;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.Role;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.AppointmentRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.ClinicalAccessService;
import br.com.fiap.petcare360_java.service.CurrentUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentSlotTest {
    private final AppointmentRepository appointments = mock(AppointmentRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final ClinicalAccessService access = mock(ClinicalAccessService.class);
    private final PetVeterinarianRepository links = mock(PetVeterinarianRepository.class);
    private final OracleClinicalProcedures procedures = mock(OracleClinicalProcedures.class);
    private final AppointmentService service = new AppointmentService(appointments, users, current, access, links, procedures);
    private final OffsetDateTime date = OffsetDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MINUTES);

    @BeforeEach
    void setUp() {
        var tutor = new AppUser(); tutor.setId(1L);
        var vet = new AppUser(); vet.setId(2L); vet.setEmail("vet@example.com");
        var pet = new Pet(); pet.setId(3L); pet.setUser(tutor);
        when(current.user()).thenReturn(tutor);
        when(access.accessiblePet(3L)).thenReturn(pet);
        when(access.userById(2L)).thenReturn(vet);
        when(links.existsActiveLink(3L, "vet@example.com")).thenReturn(true);
        when(users.lockForScheduling(2L)).thenReturn(Optional.of(vet));
    }

    @Test
    void occupiedMinuteRejectsRequestBeforeOracleProcedure() {
        when(appointments.countBusySlot(eq(2L), anyList(), eq(date), eq(date.plusMinutes(1))))
                .thenReturn(1L);

        ApiException error = assertThrows(ApiException.class,
                () -> service.request(new AppointmentRequest(3L, 2L, date, "Rotina")));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(procedures, never()).requestAppointment(anyLong(), anyLong(), anyLong(), any(), anyString());
    }

    @Test
    void freeMinuteLocksVeterinarianBeforeCreatingAppointment() {
        service.request(new AppointmentRequest(3L, 2L, date, "Rotina"));

        var order = inOrder(users, appointments, procedures);
        order.verify(users).lockForScheduling(2L);
        order.verify(appointments).countBusySlot(eq(2L), anyList(), eq(date), eq(date.plusMinutes(1)));
        order.verify(procedures).requestAppointment(3L, 1L, 2L, date, "Rotina");
    }

    @Test
    void reschedulingIgnoresTheAppointmentItselfButRejectsAnotherOne() {
        var appointment = new Appointment();
        appointment.setId(7L);
        appointment.setPet(access.accessiblePet(3L));
        appointment.setTutor(current.user());
        AppUser vet = access.userById(2L);
        vet.setName("Veterinario");
        Role role = new Role(); role.setName("ROLE_VETERINARIO");
        vet.getRoles().add(role);
        appointment.setVeterinarian(vet);
        appointment.setScheduledAt(date);
        appointment.setReason("Rotina");
        when(current.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(appointments.findVisibleById(7L)).thenReturn(Optional.of(appointment));
        when(appointments.save(appointment)).thenReturn(appointment);

        service.update(7L, new AppointmentRequest(3L, 2L, date, "Retorno"));
        verify(appointments).countBusySlotExcept(eq(2L), anyList(), eq(date), eq(date.plusMinutes(1)), eq(7L));

        when(appointments.countBusySlotExcept(eq(2L), anyList(), eq(date), eq(date.plusMinutes(1)), eq(7L)))
                .thenReturn(1L);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> service.update(7L, new AppointmentRequest(3L, 2L, date, "Retorno"))).getStatus());
    }
}
