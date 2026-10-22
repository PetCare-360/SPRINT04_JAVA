package br.com.fiap.petcare360_java;

import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import br.com.fiap.petcare360_java.model.*;
import br.com.fiap.petcare360_java.repository.*;
import br.com.fiap.petcare360_java.service.*;
import br.com.fiap.petcare360_java.exception.ApiException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentAccessTest {
    private final AppointmentRepository repository = mock(AppointmentRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final CurrentUserService currentUser = mock(CurrentUserService.class);
    private final AppointmentService service = new AppointmentService(repository, users, currentUser,
            mock(ClinicalAccessService.class), mock(PetVeterinarianRepository.class),
            mock(OracleClinicalProcedures.class));

    private Appointment fixture(long currentId) {
        var tutor = new AppUser();
        tutor.setId(1L);
        tutor.setName("Tutor");
        var vet = new AppUser();
        vet.setId(2L);
        vet.setName("Veterinario");
        var user = new AppUser();
        user.setId(currentId);
        when(currentUser.user()).thenReturn(user);
        var appointment = new Appointment();
        appointment.setId(10L);
        var pet = new Pet();
        pet.setName("Thor");
        appointment.setPet(pet);
        appointment.setTutor(tutor);
        appointment.setVeterinarian(vet);
        appointment.setScheduledAt(OffsetDateTime.now().plusDays(1));
        appointment.setReason("Consulta");
        when(repository.findVisibleById(10L)).thenReturn(Optional.of(appointment));
        when(repository.save(appointment)).thenReturn(appointment);
        when(users.lockForScheduling(2L)).thenReturn(Optional.of(vet));
        return appointment;
    }

    @Test
    void ownerCanCancelAppointmentWithoutDeletingIt() {
        var appointment = fixture(1L);
        service.delete(10L);
        assertEquals(AppointmentStatusEnum.CANCELED, appointment.getStatus());
        verify(repository).save(appointment);
        verify(repository, never()).delete(any());
    }

    @Test
    void unrelatedUserCannotReadOrRemoveAppointment() {
        fixture(3L);
        assertThrows(ApiException.class, () -> service.findEditableAppointment(10L));
        assertThrows(ApiException.class, () -> service.delete(10L));
        verify(repository, never()).delete(any());
    }

    @Test
    void veterinarianAndAdminCanCancelAppointment() {
        var appointment = fixture(2L);
        service.delete(10L);
        assertEquals(AppointmentStatusEnum.CANCELED, appointment.getStatus());
        verify(repository).save(appointment);
        reset(repository);
        appointment = fixture(3L);
        when(currentUser.hasRole("ROLE_ADMIN")).thenReturn(true);
        service.delete(10L);
        assertEquals(AppointmentStatusEnum.CANCELED, appointment.getStatus());
        verify(repository).save(appointment);
        verify(repository, never()).delete(any());
    }

    @Test
    void appointmentMustBeConfirmedBeforeItCanBeFinished() {
        var appointment = fixture(2L);
        when(currentUser.hasRole("ROLE_VETERINARIO")).thenReturn(true);
        assertThrows(ApiException.class, () -> service.finish(10L));
        service.confirm(10L);
        assertEquals(AppointmentStatusEnum.CONFIRMED, appointment.getStatus());
        service.finish(10L);
        assertEquals(AppointmentStatusEnum.DONE, appointment.getStatus());
        assertThrows(ApiException.class, () -> service.cancel(10L));
    }

    @Test
    void tutorCannotConfirmAppointment() {
        fixture(1L);
        assertThrows(ApiException.class, () -> service.confirm(10L));
    }

    @Test
    void statusFilterIsAppliedByRepositoryBeforePaging() {
        var appointment = fixture(1L);
        appointment.setStatus(AppointmentStatusEnum.CONFIRMED);
        var pageable = PageRequest.of(0, 10);
        when(currentUser.email()).thenReturn("tutor@example.com");
        when(repository.pageVisibleForUserByStatus("tutor@example.com", AppointmentStatusEnum.CONFIRMED, pageable))
                .thenReturn(new PageImpl<>(List.of(appointment), pageable, 1));

        var page = service.pageMine(pageable, AppointmentStatusEnum.CONFIRMED);

        assertEquals(1, page.getTotalElements());
        assertEquals(AppointmentStatusEnum.CONFIRMED, page.getContent().getFirst().status());
        verify(repository, never()).findVisibleForUser(anyString());
    }
}
