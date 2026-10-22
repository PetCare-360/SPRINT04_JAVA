package br.com.fiap.petcare360_java.dto;

import java.time.OffsetDateTime;

import br.com.fiap.petcare360_java.model.AlertLevelEnum;
import br.com.fiap.petcare360_java.model.AlertStatusEnum;
import br.com.fiap.petcare360_java.model.AlertTypeEnum;

public record AlertResponse(
		Long id,
		AlertTypeEnum type,
		String message,
		AlertLevelEnum level,
		AlertStatusEnum status,
		OffsetDateTime createdAt,
		OffsetDateTime resolvedAt,
		Long resolvedById) {
}
