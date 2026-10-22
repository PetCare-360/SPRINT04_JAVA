package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.dto.RegisterRequest;
import br.com.fiap.petcare360_java.service.AuthService;

/** Run only against a disposable Sprint 04 Oracle schema; the test transaction rolls back. */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "PETCARE_RUN_ORACLE_INTEGRATION", matches = "true")
class OracleClinicalProceduresIntegrationTest {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private OracleClinicalProcedures procedures;

	@Autowired
	private AuthService auth;

	@Test
	void registersTutorAndVeterinarianWithOracleRoles() {
		for (String role : new String[] { "ROLE_CLIENTE", "ROLE_VETERINARIO" }) {
			String email = "test-" + UUID.randomUUID() + "@example.com";
			auth.register(new RegisterRequest("Teste Cadastro", email, "segredo123", role));
			assertEquals(1, count("""
				SELECT COUNT(*) FROM TB_USER u
				JOIN TB_USER_ROLE ur ON ur.user_id = u.user_id
				JOIN TB_ROLE r ON r.role_id = ur.role_id
				WHERE u.email = ? AND r.name = ?
				""", email, role));
		}
	}

	@Test
	void clinicalProceduresWriteToExpectedTbTables() {
		Fixture fixture = jdbc.query("""
			SELECT pv.pet_id, p.tb_user_user_id, pv.veterinarian_id
			FROM TB_PET_VETERINARIAN pv
			JOIN TB_PET p ON p.pet_id = pv.pet_id
			JOIN TB_USER tutor ON tutor.user_id = p.tb_user_user_id
			JOIN TB_USER vet ON vet.user_id = pv.veterinarian_id
			JOIN TB_USER_ROLE ur ON ur.user_id = vet.user_id
			JOIN TB_ROLE r ON r.role_id = ur.role_id
			WHERE pv.active = 1 AND p.active = 1 AND tutor.enabled = 1 AND vet.enabled = 1
			  AND r.name = 'ROLE_VETERINARIO'
			FETCH FIRST 1 ROW ONLY
			""", (rs, row) -> new Fixture(rs.getLong(1), rs.getLong(2), rs.getLong(3)))
			.stream().findFirst().orElseThrow(() -> new AssertionError("Crie um vínculo ativo tutor/pet/veterinário no esquema de teste"));
		String marker = "TEST-" + UUID.randomUUID();

		procedures.sendMessage(fixture.petId(), fixture.tutorId(), fixture.vetId(), marker, marker);
		assertEquals(1, count("SELECT COUNT(*) FROM TB_PET_MESSAGE WHERE pet_id = ? AND subject = ?",
				fixture.petId(), marker));

		procedures.requestAppointment(fixture.petId(), fixture.tutorId(), fixture.vetId(),
				OffsetDateTime.now().plusDays(2), marker);
		assertEquals(1, count("SELECT COUNT(*) FROM TB_APPOINTMENT WHERE pet_id = ? AND reason = ?",
				fixture.petId(), marker));

		procedures.createRecommendation(fixture.petId(), fixture.tutorId(), fixture.vetId(),
				marker, marker, "NORMAL");
		assertEquals(1, count("SELECT COUNT(*) FROM TB_CARE_RECOMMENDATION WHERE pet_id = ? AND title = ?",
				fixture.petId(), marker));

		jdbc.update("""
			INSERT INTO TB_ALERT(type, message, alert_level, created_at, tb_pet_pet_id)
			VALUES ('TEMPERATURE', ?, 'WARNING', SYSTIMESTAMP, ?)
			""", marker, fixture.petId());
		long alertId = jdbc.queryForObject("SELECT alert_id FROM TB_ALERT WHERE message = ?", Long.class, marker);
		procedures.updateAlert(alertId, fixture.tutorId(), "IN_PROGRESS", marker);
		assertEquals(1, count("SELECT COUNT(*) FROM TB_ALERT WHERE alert_id = ? AND status = 'IN_PROGRESS'", alertId));
		assertEquals(1, count("SELECT COUNT(*) FROM TB_ALERT_HISTORY WHERE alert_id = ? AND note = ?",
				alertId, marker));
	}

	private int count(String sql, Object... args) {
		return jdbc.queryForObject(sql, Integer.class, args);
	}

	private record Fixture(long petId, long tutorId, long vetId) {
	}
}
