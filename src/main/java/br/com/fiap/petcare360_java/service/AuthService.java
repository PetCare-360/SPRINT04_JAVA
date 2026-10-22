package br.com.fiap.petcare360_java.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.petcare360_java.dto.AuthRequest;
import br.com.fiap.petcare360_java.dto.AuthResponse;
import br.com.fiap.petcare360_java.dto.RegisterRequest;
import br.com.fiap.petcare360_java.dto.UserResponse;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import br.com.fiap.petcare360_java.repository.OracleClinicalProcedures;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

	private final AppUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final OracleClinicalProcedures oracleClinicalProcedures;
	private final PetMapper mapper;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, OracleClinicalProcedures oracleClinicalProcedures,
			PetMapper mapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.oracleClinicalProcedures = oracleClinicalProcedures;
		this.mapper = mapper;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail");
		}

		AppUser user = new AppUser();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		String role = registerRole(request.role());
		userRepository.saveAndFlush(user);
		oracleClinicalProcedures.assignRole(user.getId(), role);
		return new AuthResponse("Usuário registrado com sucesso",
				new UserResponse(user.getId(), user.getName(), user.getEmail(), role));
	}

	private String registerRole(String role) {
		if (role == null || role.isBlank()) {
			return "ROLE_CLIENTE";
		}

		String normalizedRole = role.trim().toUpperCase();
		if ("ROLE_CLIENTE".equals(normalizedRole) || "ROLE_VETERINARIO".equals(normalizedRole)) {
			return normalizedRole;
		}

		throw new ApiException(HttpStatus.BAD_REQUEST, "Perfil permitido apenas para tutor ou veterinário");
	}

	public AuthResponse login(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		String email = request.email().trim().toLowerCase();
		var authRequest = new UsernamePasswordAuthenticationToken(email, request.password());
		Authentication authentication = authenticationManager.authenticate(authRequest);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);

		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

		return new AuthResponse("Login realizado com sucesso", mapper.toUserResponse(user));
	}
}
