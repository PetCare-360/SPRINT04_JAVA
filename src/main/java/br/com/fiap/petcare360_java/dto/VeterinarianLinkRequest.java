package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.NotNull;

public record VeterinarianLinkRequest(
		@NotNull(message = "{validation.pet.required}")
		Long petId,

		@NotNull(message = "{validation.veterinarian.required}")
		Long veterinarianId) {
}
