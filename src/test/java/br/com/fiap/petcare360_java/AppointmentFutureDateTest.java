package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Appointment;
import br.com.fiap.petcare360_java.repository.AppointmentRepository;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.service.AppointmentService;
import br.com.fiap.petcare360_java.service.ClinicalAccessService;
import br.com.fiap.petcare360_java.service.CurrentUserService;

class AppointmentFutureDateTest {
    private final AppointmentRepository appointments = mock(AppointmentRepository.class);
    private final OracleClinicalProcedures procedures = mock(OracleClinicalProcedures.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final AppointmentService service = new AppointmentService(appointments, mock(AppUserRepository.class),
            current, mock(ClinicalAccessService.class),
            mock(PetVeterinarianRepository.class), procedures);

    @Test
    void requestRejectsYesterdayBeforeCallingOracle() {
        var request = new AppointmentRequest(1L, 2L, OffsetDateTime.now().minusDays(1), "Consulta");
        ApiException error = assertThrows(ApiException.class, () -> service.request(request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(procedures, never()).requestAppointment(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void updateRejectsYesterdayBeforeSaving() {
        when(current.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(appointments.findVisibleById(10L)).thenReturn(Optional.of(new Appointment()));
        var request = new AppointmentRequest(1L, 2L, OffsetDateTime.now().minusDays(1), "Consulta");
        ApiException error = assertThrows(ApiException.class, () -> service.update(10L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(appointments, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void veterinarianUpdateAlsoRejectsYesterday() {
        AppUser veterinarian = new AppUser();
        veterinarian.setId(2L);
        AppUser tutor = new AppUser();
        tutor.setId(1L);
        Appointment appointment = new Appointment();
        appointment.setTutor(tutor);
        appointment.setVeterinarian(veterinarian);
        when(current.user()).thenReturn(veterinarian);
        when(appointments.findVisibleById(10L)).thenReturn(Optional.of(appointment));
        var request = new AppointmentRequest(1L, 2L, OffsetDateTime.now().minusDays(1), "Consulta");
        ApiException error = assertThrows(ApiException.class, () -> service.update(10L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(appointments, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
