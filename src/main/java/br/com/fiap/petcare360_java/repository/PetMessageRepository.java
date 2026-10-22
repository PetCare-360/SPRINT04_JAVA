package br.com.fiap.petcare360_java.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.petcare360_java.model.PetMessage;

public interface PetMessageRepository extends JpaRepository<PetMessage, Long> {
	@Query("""
			select count(message) from PetMessage message
			join message.sender sender join message.receiver receiver
			where lower(sender.email) = lower(:email) or lower(receiver.email) = lower(:email)
			""")
	long countVisibleForUser(@Param("email") String email);

	@Query(value = """
			select message from PetMessage message
			join fetch message.pet join fetch message.sender sender join fetch message.receiver receiver
			where lower(sender.email) = lower(:email) or lower(receiver.email) = lower(:email)
			order by message.createdAt desc, message.id desc
			""", countQuery = """
			select count(message) from PetMessage message
			join message.sender sender join message.receiver receiver
			where lower(sender.email) = lower(:email) or lower(receiver.email) = lower(:email)
			""")
	Page<PetMessage> pageVisibleForUser(@Param("email") String email, Pageable pageable);

	@Query(value = """
			select message from PetMessage message
			join fetch message.pet join fetch message.sender join fetch message.receiver
			order by message.createdAt desc, message.id desc
			""", countQuery = "select count(message) from PetMessage message")
	Page<PetMessage> pageAllVisible(Pageable pageable);
	List<PetMessage> findBySenderEmailOrReceiverEmailOrderByCreatedAtDesc(String senderEmail, String receiverEmail);

	@Query("""
			select message from PetMessage message
			join fetch message.pet pet
			join fetch message.sender sender
			join fetch message.receiver receiver
			where lower(sender.email) = lower(:email)
				or lower(receiver.email) = lower(:email)
			order by message.createdAt desc
			""")
	List<PetMessage> findVisibleForUser(@Param("email") String email);

	@Query("""
			select message from PetMessage message
			join fetch message.pet pet
			join fetch message.sender sender
			join fetch message.receiver receiver
			order by message.createdAt desc
			""")
	List<PetMessage> findAllVisible();
}
