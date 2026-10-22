package br.com.fiap.petcare360_java;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.SensorData;
import br.com.fiap.petcare360_java.service.HealthTrendService;

import static org.junit.jupiter.api.Assertions.*;

class HealthTrendServiceTest {
    private final HealthTrendService service = new HealthTrendService();

    @Test
    void ordersReadingsAndPlacesAlertOnTheSameTimeline() {
        OffsetDateTime first = OffsetDateTime.now().minusHours(2);
        SensorData older = reading(first, "38.1", 100);
        SensorData newer = reading(first.plusHours(2), "39.2", 130);
        Alert alert = new Alert();
        alert.setCreatedAt(first.plusHours(1));
        alert.setMessage("Temperatura alta");

        var trend = service.from(List.of(newer, older), List.of(alert));

        assertEquals(2, trend.temperature().points().size());
        assertEquals(52, trend.temperature().points().getFirst().x());
        assertEquals(688, trend.temperature().points().getLast().x());
        assertEquals(370, trend.alerts().getFirst().x());
        assertTrue(trend.alerts().getFirst().title().contains("Temperatura alta"));
        assertTrue(trend.heartRate().points().getLast().title().contains("130 bpm"));
    }

    @Test
    void emptyHistoryDoesNotBuildAnEmptyChart() {
        assertNull(service.from(List.of(), List.of()));
    }

    private SensorData reading(OffsetDateTime time, String temperature, int heartRate) {
        SensorData reading = new SensorData();
        reading.setTimestamp(time);
        reading.setTemperature(new BigDecimal(temperature));
        reading.setHeartRate(heartRate);
        return reading;
    }
}
