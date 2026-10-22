package br.com.fiap.petcare360_java.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MessageEventListener {
	private static final Logger log = LoggerFactory.getLogger(MessageEventListener.class);
	private final MeterRegistry meters;

	public MessageEventListener(MeterRegistry meters) {
		this.meters = meters;
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onMessageSent(MessageSentEvent event) {
		meters.counter("petcare.clinical.messages.sent").increment();
		log.info("Mensagem clínica registrada: petId={}", event.petId());
	}
}
