package com.backend.babyspa.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.babyspa.v1.config.UserDetailsServiceImpl;
import com.backend.babyspa.v1.dtos.AssignRolesDto;
import com.backend.babyspa.v1.dtos.ChangePasswordDto;
import com.backend.babyspa.v1.dtos.LoginDto;
import com.backend.babyspa.v1.dtos.LoginResponseDto;
import com.backend.babyspa.v1.dtos.RegisterUserDto;
import com.backend.babyspa.v1.exceptions.NotFoundException;
import com.backend.babyspa.v1.models.Role;
import com.backend.babyspa.v1.models.User;
import com.backend.babyspa.v1.models.UserRole;
import com.backend.babyspa.v1.models.UserRoleKey;
import com.backend.babyspa.v1.repositories.UserRepository;
import com.backend.babyspa.v1.repositories.UserRoleRepository;
import com.backend.babyspa.v1.utils.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	UserRoleRepository userRoleRepository;

	@Autowired
	RoleService roleService;

	public User findById(int userId) throws NotFoundException {

		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Nije pronađen korisnik čiji je ID: " + userId + "!"));
	}

	public User register(RegisterUserDto registerUserDto, Authentication authentication) throws Exception {

		boolean hasSuperAdminRole = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
		if (!hasSuperAdminRole) {
			throw new Exception("Ovaj korisnik nema ovlaštenje da dodjeluje uloge drugim korisnicima.");
		}

		User user = new User();

		if (userRepository.existsByUsername(registerUserDto.getUsername())) {
			throw new IllegalArgumentException("Username već postoji.");
		}

		if (userRepository.existsByEmail(registerUserDto.getEmail())) {
			throw new IllegalArgumentException("Email već postoji.");
		}

		user.setEmail(registerUserDto.getEmail());
		user.setFirstName(registerUserDto.getFirstName());
		user.setLastName(registerUserDto.getLastName());
		user.setUsername(registerUserDto.getUsername());
		user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));

		return userRepository.save(user);
	}

	public LoginResponseDto loginUser(LoginDto loginDto) throws BadCredentialsException {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (BadCredentialsException ex) {
			throw new BadCredentialsException("Pogrešni kredencijali!");
		}
		UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUsername());
		String jwt = jwtUtil.generateToken(userDetails);
		return new LoginResponseDto(jwt);
	}

	public String changePassword(ChangePasswordDto changePasswordDto, Authentication authentication) throws Exception {

		User user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen."));

		if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
			throw new Exception("Stari password nije tačan.");
		}

		user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
		userRepository.save(user);

		return "Uspješno ste promijenili password";
	}

	@Transactional
	public String assignRolesToUser(AssignRolesDto assignRolesDto, Authentication authentication) throws Exception {

		boolean hasSuperAdminRole = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
		if (!hasSuperAdminRole) {
			throw new Exception("Ovaj korisnik nema ovlaštenje da dodjeluje uloge drugim korisnicima.");
		}

		User user = findById(assignRolesDto.getUserId());

		userRoleRepository.deleteByUser(user);
		assignRolesDto.getRoleIds().forEach(roleId -> {
			UserRole userRole = new UserRole();
			Role role = roleService.findById(roleId);
			userRole.setRole(role);
			userRole.setUser(user);
			userRole.setUserRoleKey(new UserRoleKey(user.getUserId(), role.getRoleId()));
			userRoleRepository.save(userRole);
		});

		return "Uspješno ste dodijelili uloge korisniku: " + user.getUsername() + "!";
	}

}
