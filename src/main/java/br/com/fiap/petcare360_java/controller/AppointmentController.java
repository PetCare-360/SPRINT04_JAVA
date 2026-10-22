package br.com.fiap.petcare360_java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.petcare360_java.dto.AppointmentRequest;
import br.com.fiap.petcare360_java.dto.AppointmentResponse;
import br.com.fiap.petcare360_java.service.AppointmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@GetMapping
	public List<AppointmentResponse> listMine() {
		return appointmentService.listMine();
	}

	@PostMapping
	public ResponseEntity<Void> request(@RequestBody @Valid AppointmentRequest request) {
		appointmentService.request(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{id}")
	public AppointmentResponse update(@PathVariable Long id, @RequestBody @Valid AppointmentRequest request) {
		return appointmentService.update(id, request);
	}

	@PutMapping("/{id}/finish")
	public AppointmentResponse finish(@PathVariable Long id) {
		return appointmentService.finish(id);
	}

	@PutMapping("/{id}/confirm")
	public AppointmentResponse confirm(@PathVariable Long id) {
		return appointmentService.confirm(id);
	}

	@PutMapping("/{id}/cancel")
	public AppointmentResponse cancel(@PathVariable Long id) {
		return appointmentService.cancel(id);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		// Compatibilidade com clientes antigos: DELETE cancela, preservando o histórico.
		appointmentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
