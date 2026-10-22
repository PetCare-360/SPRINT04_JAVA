package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.MessageRequest;
import br.com.fiap.petcare360_java.dto.MessageResponse;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.PetMessage;
import br.com.fiap.petcare360_java.repository.PetMessageRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import jakarta.validation.Valid;

@Service
public class MessageService {

	private final PetMessageRepository messageRepository;
	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final ApplicationEventPublisher events;
	private final OracleClinicalProcedures procedures;

	public MessageService(PetMessageRepository messageRepository, CurrentUserService currentUserService,
			ClinicalAccessService clinicalAccessService, ApplicationEventPublisher events,
			OracleClinicalProcedures procedures) {
		this.messageRepository = messageRepository;
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.events = events;
		this.procedures = procedures;
	}

	@Transactional(readOnly = true)
	public List<MessageResponse> listMine() {
		String email = currentUserService.email();
		return messageRepository.findVisibleForUser(email).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MessageResponse> listAll() {
		return messageRepository.findAllVisible().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void send(@Valid MessageRequest request) {
		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser receiver = clinicalAccessService.userById(request.receiverId());
		clinicalAccessService.validateConversation(pet, receiver);

		procedures.sendMessage(pet.getId(), currentUserService.user().getId(), receiver.getId(),
				request.subject().trim(), request.message().trim());
		events.publishEvent(new MessageSentEvent(pet.getId()));
	}

	private MessageResponse toResponse(PetMessage message) {
		return new MessageResponse(
				message.getId(),
				message.getPet().getId(),
				message.getPet().getName(),
				message.getSender().getName(),
				message.getReceiver().getName(),
				message.getSubject(),
				message.getMessage(),
				message.getStatus(),
				message.getCreatedAt());
	}
}
