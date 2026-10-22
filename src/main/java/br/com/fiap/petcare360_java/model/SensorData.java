package br.com.fiap.petcare360_java.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_SENSOR_DATA")
public class SensorData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SENSOR_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TB_DEVICE_DEVICE_ID", nullable = false)
	private Device device;

	@Column(name = "DATA_HORA", nullable = false)
	private OffsetDateTime timestamp;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal temperature;

	@Column(name = "HEART_RATE", nullable = false)
	private Integer heartRate;

	@Convert(converter = ActivityLevelConverter.class)
	@Column(name = "ACTIVITY_LEVEL", nullable = false, length = 20)
	private Integer activityLevel;

	@Column(nullable = false, precision = 9, scale = 7)
	private BigDecimal latitude;

	@Column(nullable = false, precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "SENSOR_BATTERY", nullable = false)
	private Integer battery;

	@Column(name = "SENSOR_STATUS", nullable = false, length = 1)
	@JdbcTypeCode(SqlTypes.CHAR)
	private String sensorStatus = "A";

	@Enumerated(EnumType.STRING)
	@Formula("case when temperature >= 41 or heart_rate >= 160 then 'CRITICAL' when temperature > 39 or heart_rate > 130 or activity_level = 'LOW' or sensor_battery < 20 then 'WARNING' else 'NORMAL' end")
	private MonitoringStatusEnum status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public BigDecimal getTemperature() {
		return temperature;
	}

	public void setTemperature(BigDecimal temperature) {
		this.temperature = temperature;
	}

	public Integer getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(Integer heartRate) {
		this.heartRate = heartRate;
	}

	public Integer getActivityLevel() {
		return activityLevel;
	}

	public void setActivityLevel(Integer activityLevel) {
		this.activityLevel = activityLevel;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public Integer getBattery() {
		return battery;
	}

	public void setBattery(Integer battery) {
		this.battery = battery;
	}

	public MonitoringStatusEnum getStatus() {
		return status;
	}

	public void setStatus(MonitoringStatusEnum status) {
		this.status = status;
	}
}
