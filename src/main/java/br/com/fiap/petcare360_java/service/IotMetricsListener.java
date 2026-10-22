package br.com.fiap.petcare360_java.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class IotMetricsListener {
	private final MeterRegistry meters;

	public IotMetricsListener(MeterRegistry meters) {
		this.meters = meters;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onReadingSaved(IotReadingSavedEvent event) {
		meters.counter("petcare.iot.readings", "status", event.status().name()).increment();
		meters.counter("petcare.iot.alerts").increment(event.alertCount());
	}
}
