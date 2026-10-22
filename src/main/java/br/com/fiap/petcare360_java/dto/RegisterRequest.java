package br.com.fiap.petcare360_java.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "{validation.name.required}")
		@Size(max = 120, message = "{validation.name.size}")
		String name,

		@NotBlank(message = "{validation.email.required}")
		@Email(message = "{validation.email.valid}")
		@Size(max = 160, message = "{validation.email.size}")
		String email,

		@NotBlank(message = "{validation.password.required}")
		@Size(min = 6, max = 80, message = "{validation.password.size}")
		String password,

		@Size(max = 40, message = "{validation.role.size}")
		String role) {
}
