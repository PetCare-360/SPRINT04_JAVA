package br.com.fiap.petcare360_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.petcare360_java.model.AlertHistory;

public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
	@EntityGraph(attributePaths = "user")
	List<AlertHistory> findByAlertIdOrderByCreatedAtAsc(Long alertId);
}
