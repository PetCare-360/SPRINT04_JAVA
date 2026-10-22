package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppointmentRequest(
		@NotNull(message = "{validation.pet.required}")
		Long petId,

		@NotNull(message = "{validation.veterinarian.required}")
		Long veterinarianId,

		@NotNull(message = "{validation.appointment.date.required}")
		@Future(message = "{validation.appointment.future}")
		OffsetDateTime scheduledAt,

		@NotBlank(message = "{validation.reason.required}")
		@Size(max = 255, message = "{validation.reason.size}")
		String reason) {
}
