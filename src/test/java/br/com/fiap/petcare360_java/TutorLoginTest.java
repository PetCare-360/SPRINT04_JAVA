package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.model.Role;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.security.SecurityConfig;

class TutorLoginTest {
	@Test
	void springSecurityLoadsTutorRoleFromOracleUser() {
		AppUserRepository users = mock(AppUserRepository.class);
		AppUser tutor = new AppUser();
		tutor.setEmail("tutor@example.com");
		tutor.setPasswordHash("hash");
		tutor.setEnabled(true);
		Role role = new Role();
		role.setName("ROLE_CLIENTE");
		tutor.setRoles(Set.of(role));
		when(users.findByEmail("tutor@example.com")).thenReturn(Optional.of(tutor));

		var details = new SecurityConfig().userDetailsService(users)
				.loadUserByUsername(" TUTOR@EXAMPLE.COM ");

		assertEquals("tutor@example.com", details.getUsername());
		assertTrue(details.isEnabled());
		assertTrue(details.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_CLIENTE".equals(authority.getAuthority())));
	}
}
