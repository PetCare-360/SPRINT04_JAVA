package br.com.fiap.petcare360_java.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.AlertResponse;
import br.com.fiap.petcare360_java.dto.IotDataRequest;
import br.com.fiap.petcare360_java.dto.IotDataResponse;
import br.com.fiap.petcare360_java.dto.SensorDataResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.Device;
import br.com.fiap.petcare360_java.model.MonitoringStatusEnum;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.SensorData;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.DeviceRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;

@Service
public class IotProcessingService {

	private final DeviceRepository deviceRepository;
	private final SensorDataRepository sensorDataRepository;
	private final AlertRepository alertRepository;
	private final AlertPolicy alertPolicy;
	private final PetMapper mapper;
	private final ApplicationEventPublisher events;

	public IotProcessingService(DeviceRepository deviceRepository, SensorDataRepository sensorDataRepository,
			AlertRepository alertRepository, AlertPolicy alertPolicy, PetMapper mapper,
			ApplicationEventPublisher events) {
		this.deviceRepository = deviceRepository;
		this.sensorDataRepository = sensorDataRepository;
		this.alertRepository = alertRepository;
		this.alertPolicy = alertPolicy;
		this.mapper = mapper;
		this.events = events;
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public IotDataResponse process(IotDataRequest request) {
		Device device = deviceRepository.findByDeviceCode(request.deviceId().trim())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Device não encontrado"));

		if (!device.isActive()) {
			throw new ApiException(HttpStatus.CONFLICT, "Device inativo para recebimento de telemetria");
		}

		Pet pet = device.getPet();
		if (pet == null) {
			throw new ApiException(HttpStatus.CONFLICT, "Device não está vinculado a um pet");
		}

		AlertPolicy.Evaluation evaluation = alertPolicy.evaluate(pet, request.temperature(), request.heartRate(),
				request.activityLevel(), request.battery());
		MonitoringStatusEnum status = evaluation.status();
		SensorData sensorData = new SensorData();
		sensorData.setDevice(device);
		sensorData.setTimestamp(request.timestamp());
		sensorData.setTemperature(request.temperature());
		sensorData.setHeartRate(request.heartRate());
		sensorData.setActivityLevel(request.activityLevel());
		sensorData.setLatitude(request.latitude());
		sensorData.setLongitude(request.longitude());
		sensorData.setBattery(request.battery());
		sensorData.setStatus(status);
		sensorDataRepository.save(sensorData);

		device.updateTelemetry(request.battery(), request.timestamp());
		deviceRepository.save(device);

		List<AlertResponse> alerts = alertRepository.saveAll(evaluation.alerts()).stream()
				.map(mapper::toAlertResponse)
				.toList();
		events.publishEvent(new IotReadingSavedEvent(status, alerts.size()));

		return new IotDataResponse(
				sensorData.getId(),
				device.getDeviceCode(),
				pet.getId(),
				status,
				OffsetDateTime.now(),
				alerts);
	}

	@Transactional(readOnly = true)
	public List<SensorDataResponse> listReadings() {
		return sensorDataRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).stream()
				.map(mapper::toSensorDataResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SensorDataResponse findReading(Long id) {
		return mapper.toSensorDataResponse(findSensorData(id));
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public SensorDataResponse updateReading(Long id, IotDataRequest request) {
		SensorData sensorData = findSensorData(id);
		Device device = deviceRepository.findByDeviceCode(request.deviceId().trim())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Device não encontrado"));

		sensorData.setDevice(device);
		sensorData.setTimestamp(request.timestamp());
		sensorData.setTemperature(request.temperature());
		sensorData.setHeartRate(request.heartRate());
		sensorData.setActivityLevel(request.activityLevel());
		sensorData.setLatitude(request.latitude());
		sensorData.setLongitude(request.longitude());
		sensorData.setBattery(request.battery());
		sensorData.setStatus(alertPolicy.evaluate(device.getPet(), request.temperature(), request.heartRate(),
				request.activityLevel(), request.battery()).status());

		device.updateTelemetry(request.battery(), request.timestamp());
		deviceRepository.save(device);

		return mapper.toSensorDataResponse(sensorDataRepository.save(sensorData));
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary", "petMonitoring", "petActivity", "petLocation", "petAlerts", "petHealth", "petQuickAlerts", "petActivitySummary" }, allEntries = true)
	public void deleteReading(Long id) {
		sensorDataRepository.delete(findSensorData(id));
	}

	private SensorData findSensorData(Long id) {
		return sensorDataRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Leitura IoT não encontrada"));
	}

}
