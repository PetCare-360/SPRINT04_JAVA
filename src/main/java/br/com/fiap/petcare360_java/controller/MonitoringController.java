package br.com.fiap.petcare360_java.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.AlertResponse;
import br.com.fiap.petcare360_java.dto.AlertHistoryResponse;
import br.com.fiap.petcare360_java.dto.AlertStatusUpdateRequest;
import br.com.fiap.petcare360_java.dto.PetSummaryResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.service.MonitoringService;
import br.com.fiap.petcare360_java.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/pets/{id}")
public class MonitoringController {

	private final MonitoringService monitoringService;
	private final AlertService alertService;

	public MonitoringController(MonitoringService monitoringService, AlertService alertService) {
		this.monitoringService = monitoringService;
		this.alertService = alertService;
	}

	@Operation(
			summary = "Resumo atual do pet",
			description = "Retorna o cadastro do pet, a última leitura de sensor, o status atual, total de leituras e total de alertas.")
	@GetMapping("/summary")
	public PetSummaryResponse summary(@PathVariable Long id) {
		return monitoringService.summary(id);
	}

	@Operation(
			summary = "Histórico de monitoramento",
			description = "Lista leituras históricas de sensor com paginação simples, sempre da mais recente para a mais antiga.")
	@GetMapping("/monitoring")
	public Page<SensorDataResponse> monitoring(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") Integer size) {
		return monitoringService.monitoring(id, null, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
	}

	@Operation(
			summary = "Histórico de atividade",
			description = "Lista as leituras para análise de atividade do pet com paginação e ordenação.")
	@GetMapping("/activity")
	public Page<SensorDataResponse> activity(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") Integer size) {
		return monitoringService.activity(id, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
	}

	@Operation(
			summary = "Última localização",
			description = "Retorna a leitura mais recente com latitude e longitude do pet.")
	@GetMapping("/location")
	public SensorDataResponse location(@PathVariable Long id) {
		return monitoringService.location(id);
	}

	@Operation(
			summary = "Alertas do pet",
			description = "Lista alertas gerados automaticamente para o pet com paginação simples.")
	@GetMapping("/alerts")
	public Page<AlertResponse> alerts(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") Integer size) {
		return monitoringService.alerts(id, null, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
	}

	@Operation(summary = "Atualizar status do alerta",
			description = "Aplica o fluxo OPEN -> IN_PROGRESS -> RESOLVED e registra a alteração no histórico.")
	@PatchMapping("/alerts/{alertId}/status")
	public AlertResponse updateAlertStatus(@PathVariable Long id, @PathVariable Long alertId,
			@RequestBody @Valid AlertStatusUpdateRequest request) {
		return alertService.updateStatus(id, alertId, request);
	}

	@Operation(summary = "Histórico do alerta")
	@GetMapping("/alerts/{alertId}/history")
	public List<AlertHistoryResponse> alertHistory(@PathVariable Long id, @PathVariable Long alertId) {
		return alertService.history(id, alertId);
	}
}
