package br.com.fiap.petcare360_java.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_ALERT_HISTORY")
public class AlertHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "HISTORY_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ALERT_ID", nullable = false)
	private Alert alert;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USER_ID", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "PREVIOUS_STATUS", length = 20)
	private AlertStatusEnum previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "NEW_STATUS", nullable = false, length = 20)
	private AlertStatusEnum newStatus;

	@Column(length = 255)
	private String note;

	@Column(name = "CREATED_AT", nullable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Alert getAlert() { return alert; }
	public void setAlert(Alert alert) { this.alert = alert; }
	public AppUser getUser() { return user; }
	public void setUser(AppUser user) { this.user = user; }
	public AlertStatusEnum getPreviousStatus() { return previousStatus; }
	public void setPreviousStatus(AlertStatusEnum previousStatus) { this.previousStatus = previousStatus; }
	public AlertStatusEnum getNewStatus() { return newStatus; }
	public void setNewStatus(AlertStatusEnum newStatus) { this.newStatus = newStatus; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
