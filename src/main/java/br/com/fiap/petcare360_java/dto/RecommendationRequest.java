package br.com.fiap.petcare360_java.dto;

import br.com.fiap.petcare360_java.model.RecommendationPriorityEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecommendationRequest(
		@NotNull(message = "{validation.pet.required}")
		Long petId,

		@NotBlank(message = "{validation.title.required}")
		@Size(max = 120, message = "{validation.title.size}")
		String title,

		@NotBlank(message = "{validation.instructions.required}")
		@Size(max = 1000, message = "{validation.instructions.size}")
		String instructions,

		@NotNull(message = "{validation.priority.required}")
		RecommendationPriorityEnum priority) {
}
