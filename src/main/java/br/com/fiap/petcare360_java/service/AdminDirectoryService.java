package br.com.fiap.petcare360_java.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Pet;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.PetRepository;

@Service
public class AdminDirectoryService {
	private final AppUserRepository users;
	private final PetRepository pets;

	public AdminDirectoryService(AppUserRepository users, PetRepository pets) {
		this.users = users;
		this.pets = pets;
	}

	@Transactional(readOnly = true)
	public List<AppUser> users(String role) {
		return role == null || role.isBlank() ? users.findAll() : users.findByRolesName(role);
	}

	@Transactional(readOnly = true)
	public List<Pet> pets(String active) {
		if ("ativos".equals(active)) return pets.findByActiveTrue();
		if ("inativos".equals(active)) return pets.findByActiveFalse();
		return pets.findAll();
	}
}
