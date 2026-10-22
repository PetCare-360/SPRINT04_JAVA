package br.com.fiap.petcare360_java.service;

import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;

public record IotReadingSavedEvent(MonitoringStatusEnum status, int alertCount) {
}
