package br.com.fiap.petcare360_java.repository;

import java.util.List;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.petcare360_java.model.Appointment;
import br.com.fiap.petcare360_java.model.AppointmentStatusEnum;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
	@Query("""
			select count(appointment) from Appointment appointment
			where appointment.veterinarian.id = :veterinarianId
				and appointment.status in :activeStatuses
				and appointment.scheduledAt >= :start and appointment.scheduledAt < :end
			""")
	long countBusySlot(@Param("veterinarianId") Long veterinarianId,
			@Param("activeStatuses") List<AppointmentStatusEnum> activeStatuses,
			@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

	@Query("""
			select count(appointment) from Appointment appointment
			where appointment.veterinarian.id = :veterinarianId
				and appointment.status in :activeStatuses
				and appointment.scheduledAt >= :start and appointment.scheduledAt < :end
				and appointment.id <> :excludedId
			""")
	long countBusySlotExcept(@Param("veterinarianId") Long veterinarianId,
			@Param("activeStatuses") List<AppointmentStatusEnum> activeStatuses,
			@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end,
			@Param("excludedId") Long excludedId);

	@Query("""
			select count(appointment) from Appointment appointment
			join appointment.tutor tutor join appointment.veterinarian veterinarian
			where lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email)
			""")
	long countVisibleForUser(@Param("email") String email);

	@Query(value = """
			select appointment from Appointment appointment
			join fetch appointment.pet join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			where lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email)
			order by appointment.scheduledAt desc, appointment.id desc
			""", countQuery = """
			select count(appointment) from Appointment appointment
			join appointment.tutor tutor join appointment.veterinarian veterinarian
			where lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email)
			""")
	Page<Appointment> pageVisibleForUser(@Param("email") String email, Pageable pageable);

	@Query(value = """
			select appointment from Appointment appointment
			join fetch appointment.pet join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			where (lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email))
				and appointment.status = :status
			order by appointment.scheduledAt desc, appointment.id desc
			""", countQuery = """
			select count(appointment) from Appointment appointment
			join appointment.tutor tutor join appointment.veterinarian veterinarian
			where (lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email))
				and appointment.status = :status
			""")
	Page<Appointment> pageVisibleForUserByStatus(@Param("email") String email,
			@Param("status") AppointmentStatusEnum status, Pageable pageable);

	@Query(value = """
			select appointment from Appointment appointment
			join fetch appointment.pet join fetch appointment.tutor join fetch appointment.veterinarian
			order by appointment.scheduledAt desc, appointment.id desc
			""", countQuery = "select count(appointment) from Appointment appointment")
	Page<Appointment> pageAllVisible(Pageable pageable);

	@Query(value = """
			select appointment from Appointment appointment
			join fetch appointment.pet join fetch appointment.tutor join fetch appointment.veterinarian
			where appointment.status = :status
			order by appointment.scheduledAt desc, appointment.id desc
			""", countQuery = """
			select count(appointment) from Appointment appointment where appointment.status = :status
			""")
	Page<Appointment> pageAllVisibleByStatus(@Param("status") AppointmentStatusEnum status, Pageable pageable);
	List<Appointment> findByTutorEmailOrVeterinarianEmailOrderByScheduledAtDesc(String tutorEmail, String veterinarianEmail);

	@Query("""
			select appointment from Appointment appointment
			join fetch appointment.pet pet
			join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			where appointment.id = :id
			""")
	java.util.Optional<Appointment> findVisibleById(@Param("id") Long id);

	@Query("""
			select appointment from Appointment appointment
			join fetch appointment.pet pet
			join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			where lower(tutor.email) = lower(:email)
				or lower(veterinarian.email) = lower(:email)
			order by appointment.scheduledAt desc
			""")
	List<Appointment> findVisibleForUser(@Param("email") String email);

	@Query("""
			select appointment from Appointment appointment
			join fetch appointment.pet pet
			join fetch appointment.tutor tutor
			join fetch appointment.veterinarian veterinarian
			order by appointment.scheduledAt desc
			""")
	List<Appointment> findAllVisible();
}
