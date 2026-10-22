package br.com.fiap.petcare360_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.VeterinarianLinkRequest;
import br.com.fiap.petcare360_java.dto.VeterinarianLinkResponse;
import br.com.fiap.petcare360_java.service.VeterinarianLinkService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/veterinarians")
public class VeterinarianLinkController {

	private final VeterinarianLinkService veterinarianLinkService;

	public VeterinarianLinkController(VeterinarianLinkService veterinarianLinkService) {
		this.veterinarianLinkService = veterinarianLinkService;
	}

	@PostMapping("/link")
	public ResponseEntity<VeterinarianLinkResponse> link(@RequestBody @Valid VeterinarianLinkRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(veterinarianLinkService.link(request));
	}
}
