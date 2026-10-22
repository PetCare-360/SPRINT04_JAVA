package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.RecommendationRequest;
import br.com.fiap.petcare360_java.dto.RecommendationResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.CareRecommendation;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.CareRecommendationRepository;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;

@Service
public class RecommendationService {

	private final CareRecommendationRepository recommendationRepository;
	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final PetVeterinarianRepository petVeterinarianRepository;
	private final OracleClinicalProcedures procedures;

	public RecommendationService(CareRecommendationRepository recommendationRepository,
			CurrentUserService currentUserService, ClinicalAccessService clinicalAccessService,
			PetVeterinarianRepository petVeterinarianRepository, OracleClinicalProcedures procedures) {
		this.recommendationRepository = recommendationRepository;
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.petVeterinarianRepository = petVeterinarianRepository;
		this.procedures = procedures;
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> listMine() {
		String email = currentUserService.email();
		return recommendationRepository.findVisibleForUser(email).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> listAll() {
		return recommendationRepository.findAllVisible().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void create(RecommendationRequest request) {
		if (!currentUserService.hasRole("ROLE_VETERINARIO")) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Apenas veterinários podem enviar recomendações");
		}

		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser veterinarian = currentUserService.user();

		if (!petVeterinarianRepository.existsActiveLink(pet.getId(), veterinarian.getEmail())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Veterinário não vinculado ao pet");
		}

		procedures.createRecommendation(pet.getId(), pet.getUser().getId(), veterinarian.getId(),
				request.title().trim(), request.instructions().trim(), request.priority().name());
	}

	private RecommendationResponse toResponse(CareRecommendation recommendation) {
		return new RecommendationResponse(
				recommendation.getId(),
				recommendation.getPet().getName(),
				recommendation.getTutor().getName(),
				recommendation.getVeterinarian().getName(),
				recommendation.getTitle(),
				recommendation.getInstructions(),
				recommendation.getPriority(),
				recommendation.getStatus(),
				recommendation.getCreatedAt());
	}
}
