package br.com.fiap.petcare360_java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ORACLE_DB_URL", matches = ".+")
class Petcare360JavaApplicationTests {
	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void contextLoads() {
	}

	@Test
	void flywayHistoryContainsBaselineAndSchemaVerification() {
		Integer count = jdbc.queryForObject(
				"SELECT COUNT(*) FROM FLYWAY_SCHEMA_HISTORY WHERE VERSION IN ('1', '2') AND SUCCESS = 1",
				Integer.class);
		assertEquals(2, count);
	}

}
