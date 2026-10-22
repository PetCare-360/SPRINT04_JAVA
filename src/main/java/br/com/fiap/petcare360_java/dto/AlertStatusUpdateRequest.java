package br.com.fiap.petcare360_java.dto;

import br.com.fiap.petcare360_java.model.AlertStatusEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertStatusUpdateRequest(
		@NotNull AlertStatusEnum status,
		@Size(max = 255) String note) {
}
