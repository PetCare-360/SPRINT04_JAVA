package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.AlertStatusEnum;

public record AlertHistoryResponse(
		Long id,
		AlertStatusEnum previousStatus,
		AlertStatusEnum newStatus,
		String note,
		Long userId,
		String userName,
		OffsetDateTime createdAt) {
}
