package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
		@NotBlank(message = "{validation.email.required}")
		@Email(message = "{validation.email.valid}")
		String email,

		@NotBlank(message = "{validation.password.required}")
		String password) {
}
