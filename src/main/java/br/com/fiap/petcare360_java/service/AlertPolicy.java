package br.com.fiap.petcare360_java.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.AlertLevelEnum;
import br.com.fiap.petcare360_java.model.AlertTypeEnum;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;

@Component
public class AlertPolicy {

	private static final BigDecimal TEMPERATURE_ALERT = new BigDecimal("39.0");
	private static final BigDecimal TEMPERATURE_CRITICAL = new BigDecimal("41.0");

	public Evaluation evaluate(Pet pet, BigDecimal temperature, int heartRate, int activityLevel, Integer battery) {
		List<Alert> alerts = new ArrayList<>();

		if (temperature.compareTo(TEMPERATURE_CRITICAL) >= 0) {
			alerts.add(alert(pet, AlertTypeEnum.TEMPERATURE, AlertLevelEnum.CRITICAL,
					"Temperatura crítica detectada: " + temperature + " °C"));
		} else if (temperature.compareTo(TEMPERATURE_ALERT) > 0) {
			alerts.add(alert(pet, AlertTypeEnum.TEMPERATURE, AlertLevelEnum.WARNING,
					"Temperatura acima do ideal: " + temperature + " °C"));
		}

		if (heartRate >= 160) {
			alerts.add(alert(pet, AlertTypeEnum.HEART_RATE, AlertLevelEnum.CRITICAL,
					"Batimentos cardíacos em nível crítico: " + heartRate + " bpm"));
		} else if (heartRate > 130) {
			alerts.add(alert(pet, AlertTypeEnum.HEART_RATE, AlertLevelEnum.WARNING,
					"Batimentos cardíacos elevados: " + heartRate + " bpm"));
		}

		if (activityLevel < 34) {
			alerts.add(alert(pet, AlertTypeEnum.ACTIVITY, AlertLevelEnum.WARNING,
					"Nível de atividade muito baixo: " + activityLevel + "%"));
		}

		if (battery != null && battery < 20) {
			alerts.add(alert(pet, AlertTypeEnum.BATTERY, AlertLevelEnum.WARNING,
					"Bateria baixa da coleira: " + battery + "%"));
		}

		MonitoringStatusEnum status = alerts.stream().anyMatch(a -> a.getLevel() == AlertLevelEnum.CRITICAL)
				? MonitoringStatusEnum.CRITICAL
				: alerts.isEmpty() ? MonitoringStatusEnum.NORMAL : MonitoringStatusEnum.WARNING;

		return new Evaluation(status, List.copyOf(alerts));
	}

	private Alert alert(Pet pet, AlertTypeEnum type, AlertLevelEnum level, String message) {
		Alert alert = new Alert();
		alert.setPet(pet);
		alert.setType(type);
		alert.setLevel(level);
		alert.setMessage(message);
		return alert;
	}

	public record Evaluation(MonitoringStatusEnum status, List<Alert> alerts) {
	}
}
