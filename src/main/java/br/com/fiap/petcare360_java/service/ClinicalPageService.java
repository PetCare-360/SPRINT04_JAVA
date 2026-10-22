package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.model.Alert;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.PetVeterinarian;
import br.com.fiap.petcare360_java.model.SensorData;
import br.com.fiap.petcare360_java.repository.AlertRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.repository.SensorDataRepository;

@Service
public class ClinicalPageService {
	private final PetService pets;
	private final CurrentUserService currentUser;
	private final PetVeterinarianRepository links;
	private final SensorDataRepository readings;
	private final AlertRepository alerts;

	public ClinicalPageService(PetService pets, CurrentUserService currentUser,
			PetVeterinarianRepository links, SensorDataRepository readings, AlertRepository alerts) {
		this.pets = pets;
		this.currentUser = currentUser;
		this.links = links;
		this.readings = readings;
		this.alerts = alerts;
	}

	@Transactional(readOnly = true)
	public PetDetail detail(Long petId) {
		Pet pet = pets.findAccessiblePet(petId);
		return new PetDetail(pet,
				readings.findFirstByDevicePetIdOrderByTimestampDesc(petId).orElse(null),
				readings.findTop30ByDevicePetIdOrderByTimestampDesc(petId),
				alerts.findByPetIdOrderByCreatedAtDesc(petId), links.findByPetId(petId));
	}

	@Transactional(readOnly = true)
	public List<PetVeterinarian> appointmentOptions() {
		if (currentUser.hasRole("ROLE_ADMIN")) return links.findAll();
		if (currentUser.hasRole("ROLE_VETERINARIO")) {
			return links.findActiveLinksByVeterinarianEmail(currentUser.email());
		}
		return links.findActiveLinksByTutorEmail(currentUser.email());
	}

	public record PetDetail(Pet pet, SensorData latestReading, List<SensorData> readings,
			List<Alert> alerts, List<PetVeterinarian> links) {
	}
}
