package br.com.fiap.petcare360_java.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import br.com.fiap.petcare360_java.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select account from AppUser account where account.id = :id")
	Optional<AppUser> lockForScheduling(@Param("id") Long id);
	Optional<AppUser> findByEmail(String email);
	boolean existsByEmail(String email);

	@EntityGraph(attributePaths = "roles")
	List<AppUser> findAll();

	@EntityGraph(attributePaths = "roles")
	List<AppUser> findByRolesName(String roleName);
}
