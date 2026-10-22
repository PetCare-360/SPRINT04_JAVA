package br.com.fiap.petcare360_java.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.SensorData;

@Service
public class HealthTrendService {
	private static final int LEFT = 52;
	private static final int RIGHT = 688;
	private static final int TOP = 28;
	private static final int BOTTOM = 183;
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public HealthTrend from(List<SensorData> recentReadings, List<Alert> recentAlerts) {
		List<SensorData> readings = recentReadings.stream()
				.filter(reading -> reading.getTimestamp() != null && reading.getTemperature() != null
						&& reading.getHeartRate() != null)
				.sorted(Comparator.comparing(SensorData::getTimestamp)).toList();
		if (readings.isEmpty()) return null;

		long first = readings.getFirst().getTimestamp().toInstant().toEpochMilli();
		long last = readings.getLast().getTimestamp().toInstant().toEpochMilli();
		List<AlertMarker> markers = recentAlerts.stream()
				.filter(alert -> alert.getCreatedAt() != null)
				.filter(alert -> {
					long time = alert.getCreatedAt().toInstant().toEpochMilli();
					return time >= first && time <= last;
				})
				.map(alert -> new AlertMarker(x(alert.getCreatedAt(), first, last),
						alert.getCreatedAt().format(DATE) + " — " + alert.getMessage()))
				.toList();
		return new HealthTrend(
				chart(readings, first, last, reading -> reading.getTemperature().doubleValue(), "°C", 1),
				chart(readings, first, last, reading -> reading.getHeartRate(), "bpm", 0),
				markers, readings.getFirst().getTimestamp().format(DATE),
				readings.getLast().getTimestamp().format(DATE));
	}

	private MetricChart chart(List<SensorData> readings, long first, long last,
			ToDoubleFunction<SensorData> value, String unit, int decimals) {
		double min = readings.stream().mapToDouble(value).min().orElse(0);
		double max = readings.stream().mapToDouble(value).max().orElse(0);
		double padding = Math.max((max - min) * 0.15, decimals == 0 ? 5 : 0.5);
		double low = min - padding;
		double high = max + padding;
		List<PlotPoint> points = readings.stream().map(reading -> {
			double number = value.applyAsDouble(reading);
			int y = BOTTOM - (int) Math.round((number - low) / (high - low) * (BOTTOM - TOP));
			String label = reading.getTimestamp().format(DATE) + " — "
					+ String.format(Locale.ROOT, "%1$." + decimals + "f", number) + " " + unit;
			return new PlotPoint(x(reading.getTimestamp(), first, last), y, label);
		}).toList();
		return new MetricChart(points.stream().map(point -> point.x() + "," + point.y())
				.collect(Collectors.joining(" ")), points,
				String.format(Locale.ROOT, "%1$." + decimals + "f", low),
				String.format(Locale.ROOT, "%1$." + decimals + "f", high));
	}

	private int x(OffsetDateTime time, long first, long last) {
		if (first == last) return (LEFT + RIGHT) / 2;
		return LEFT + (int) Math.round((time.toInstant().toEpochMilli() - first)
				/ (double) (last - first) * (RIGHT - LEFT));
	}

	public record HealthTrend(MetricChart temperature, MetricChart heartRate,
			List<AlertMarker> alerts, String firstDate, String lastDate) { }
	public record MetricChart(String polyline, List<PlotPoint> points, String minLabel, String maxLabel) { }
	public record PlotPoint(int x, int y, String title) { }
	public record AlertMarker(int x, String title) { }
}
