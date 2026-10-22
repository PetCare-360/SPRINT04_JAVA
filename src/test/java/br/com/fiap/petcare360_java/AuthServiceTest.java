package br.com.fiap.petcare360_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.fiap.petcare360_java.dto.RegisterRequest;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import br.com.fiap.petcare360_java.service.AuthService;
import br.com.fiap.petcare360_java.service.PetMapper;

class AuthServiceTest {
	@Test
	void registrationUsesOracleProcedureForRoleWithoutJpaJoinInsert() {
		AppUserRepository users = mock(AppUserRepository.class);
		OracleClinicalProcedures procedures = mock(OracleClinicalProcedures.class);
		PasswordEncoder passwords = mock(PasswordEncoder.class);
		when(passwords.encode("segredo123")).thenReturn("hashed");
		when(users.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> {
			AppUser user = invocation.getArgument(0);
			user.setId(1001L);
			return user;
		});

		AuthService service = new AuthService(users, passwords, mock(AuthenticationManager.class),
				procedures, new PetMapper());
		var response = service.register(new RegisterRequest("Ana", " ANA@EXAMPLE.COM ", "segredo123", "ROLE_CLIENTE"));

		verify(procedures).assignRole(1001L, "ROLE_CLIENTE");
		assertEquals("ana@example.com", response.user().email());
		assertEquals("ROLE_CLIENTE", response.user().role());
	}
}
