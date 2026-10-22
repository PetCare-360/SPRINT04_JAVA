package br.com.fiap.petcare360_java.repository;

import java.sql.CallableStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Calls the PL/SQL rules supplied by the shared TB_* schema. */
@Repository
public class OracleClinicalProcedures {

	private final JdbcTemplate jdbcTemplate;

	public OracleClinicalProcedures(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void linkVeterinarian(long petId, long veterinarianId) {
		execute("{call PKG_PETCARE360_CLINICO.PRC_VINCULAR_VETERINARIO(?, ?)}", petId, veterinarianId);
	}

	public void assignRole(long userId, String role) {
		execute("{call PKG_PETCARE360_CLINICO.PRC_ATRIBUIR_PERFIL(?, ?)}", userId, role);
	}

	public void updateAlert(long alertId, long actorId, String status, String note) {
		execute("{call PKG_PETCARE360_CLINICO.PRC_ATUALIZAR_ALERTA(?, ?, ?, ?)}", alertId, actorId, status, note);
	}

	public void sendMessage(long petId, long senderId, long receiverId, String subject, String message) {
		execute("{call PKG_PETCARE360_CLINICO.PRC_ENVIAR_MENSAGEM(?, ?, ?, ?, ?)}",
				petId, senderId, receiverId, subject, message);
	}

	public void requestAppointment(long petId, long tutorId, long veterinarianId,
			OffsetDateTime scheduledAt, String reason) {
		execute("{call PKG_PETCARE360_CLINICO.PRC_AGENDAR_CONSULTA(?, ?, ?, ?, ?)}",
				petId, tutorId, veterinarianId, Timestamp.from(scheduledAt.toInstant()), reason);
	}

	public void createRecommendation(long petId, long tutorId, long veterinarianId,
			String title, String instructions, String priority) {
		execute("{call PKG_PETCARE360_CLINICO.PRC_CRIAR_RECOMENDACAO(?, ?, ?, ?, ?, ?)}",
				petId, tutorId, veterinarianId, title, instructions, priority);
	}

	private void execute(String call, Object... values) {
		jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
			try (CallableStatement statement = connection.prepareCall(call)) {
				for (int index = 0; index < values.length; index++) {
					if (values[index] == null) {
						statement.setNull(index + 1, Types.VARCHAR);
					} else {
						statement.setObject(index + 1, values[index]);
					}
				}
				statement.execute();
			}
			return null;
		});
	}
}
