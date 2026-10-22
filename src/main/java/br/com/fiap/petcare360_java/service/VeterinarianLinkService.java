package br.com.fiap.petcare360_java.service;

import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.VeterinarianLinkRequest;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.model.PetVeterinarian;
import br.com.fiap.petcare360_java.model.PetVeterinarianId;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.repository.PetVeterinarianRepository;

@Service
public class VeterinarianLinkService {

	private final CurrentUserService currentUserService;
	private final ClinicalAccessService clinicalAccessService;
	private final PetVeterinarianRepository petVeterinarianRepository;
	private final OracleClinicalProcedures oracleClinicalProcedures;

	public VeterinarianLinkService(CurrentUserService currentUserService, ClinicalAccessService clinicalAccessService,
			PetVeterinarianRepository petVeterinarianRepository, OracleClinicalProcedures oracleClinicalProcedures) {
		this.currentUserService = currentUserService;
		this.clinicalAccessService = clinicalAccessService;
		this.petVeterinarianRepository = petVeterinarianRepository;
		this.oracleClinicalProcedures = oracleClinicalProcedures;
	}

	@Transactional
	@CacheEvict(value = { "pets", "pet", "petSummary" }, allEntries = true)
	public VeterinarianLinkResponse link(VeterinarianLinkRequest request) {
		Pet pet = clinicalAccessService.accessiblePet(request.petId());
		AppUser tutor = currentUserService.user();
		if (!pet.getUser().getId().equals(tutor.getId()) && !currentUserService.hasRole("ROLE_ADMIN")) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Apenas o tutor pode vincular veterinário ao pet");
		}

		AppUser veterinarian = clinicalAccessService.userById(request.veterinarianId());
		boolean isVet = veterinarian.getRoles().stream()
				.anyMatch(role -> "ROLE_VETERINARIO".equals(role.getName()));
		if (!isVet) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Usuário selecionado não possui perfil de veterinário");
		}

		if (petVeterinarianRepository.existsActiveLink(pet.getId(), veterinarian.getEmail())) {
			throw new ApiException(HttpStatus.CONFLICT, "Veterinário já vinculado a este pet");
		}

		oracleClinicalProcedures.linkVeterinarian(pet.getId(), veterinarian.getId());
		PetVeterinarianId id = new PetVeterinarianId();
		id.setPetId(pet.getId());
		id.setVeterinarianId(veterinarian.getId());
		PetVeterinarian saved = petVeterinarianRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Vínculo não encontrado após execução da procedure"));
		return toResponse(saved);
	}

	private VeterinarianLinkResponse toResponse(PetVeterinarian link) {
		return new VeterinarianLinkResponse(
				link.getPet().getId(),
				link.getPet().getName(),
				link.getVeterinarian().getId(),
				link.getVeterinarian().getName(),
				link.getAssignedAt(),
				link.getActive());
	}
}
