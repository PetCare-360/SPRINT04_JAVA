package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IotDataRequest(
		@NotBlank(message = "{validation.deviceId.required}")
		@Size(max = 80, message = "{validation.deviceId.size}")
		String deviceId,

		@NotNull(message = "{validation.reading.time.required}")
		OffsetDateTime timestamp,

		@NotNull(message = "{validation.temperature.required}")
		@DecimalMin(value = "30.0", message = "{validation.temperature.range}")
		@DecimalMax(value = "45.0", message = "{validation.temperature.range}")
		BigDecimal temperature,

		@NotNull(message = "{validation.heartRate.required}")
		@Min(value = 40, message = "{validation.heartRate.range}")
		@Max(value = 220, message = "{validation.heartRate.range}")
		Integer heartRate,

		@NotNull(message = "{validation.activity.required}")
		@Min(value = 0, message = "{validation.activity.range}")
		@Max(value = 100, message = "{validation.activity.range}")
		Integer activityLevel,

		@NotNull(message = "{validation.latitude.required}")
		@DecimalMin(value = "-90.0", message = "{validation.latitude.range}")
		@DecimalMax(value = "90.0", message = "{validation.latitude.range}")
		BigDecimal latitude,

		@NotNull(message = "{validation.longitude.required}")
		@DecimalMin(value = "-180.0", message = "{validation.longitude.range}")
		@DecimalMax(value = "180.0", message = "{validation.longitude.range}")
		BigDecimal longitude,

		@NotNull(message = "{validation.battery.required}")
		@Min(value = 0, message = "{validation.battery.range}")
		@Max(value = 100, message = "{validation.battery.range}")
		Integer battery) {
}
