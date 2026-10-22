package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
		@NotNull(message = "{validation.pet.required}")
		Long petId,

		@NotNull(message = "{validation.receiver.required}")
		Long receiverId,

		@NotBlank(message = "{validation.subject.required}")
		@Size(max = 120, message = "{validation.subject.size}")
		String subject,

		@NotBlank(message = "{validation.message.required}")
		@Size(max = 1000, message = "{validation.message.size}")
		String message) {
}
