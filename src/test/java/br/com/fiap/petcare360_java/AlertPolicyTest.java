package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.model.AlertLevelEnum;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.service.AlertPolicy;

class AlertPolicyTest {

	private final AlertPolicy policy = new AlertPolicy();
	private final Pet pet = new Pet();

	@Test
	void normalMetricsDoNotGenerateAlerts() {
		var result = policy.evaluate(pet, new BigDecimal("38.5"), 110, 70, 80);
		assertEquals(MonitoringStatusEnum.NORMAL, result.status());
		assertEquals(0, result.alerts().size());
	}

	@Test
	void warningMetricsGenerateAllApplicableAlerts() {
		var result = policy.evaluate(pet, new BigDecimal("39.5"), 140, 10, 10);
		assertEquals(MonitoringStatusEnum.WARNING, result.status());
		assertEquals(4, result.alerts().size());
	}

	@Test
	void criticalMetricDeterminesOverallStatus() {
		var result = policy.evaluate(pet, new BigDecimal("41.0"), 110, 70, 80);
		assertEquals(MonitoringStatusEnum.CRITICAL, result.status());
		assertEquals(AlertLevelEnum.CRITICAL, result.alerts().getFirst().getLevel());
	}
}
