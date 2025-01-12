package com.backend.babyspa.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.babyspa.v1.config.UserDetailsServiceImpl;
import com.backend.babyspa.v1.dtos.LoginDto;
import com.backend.babyspa.v1.dtos.LoginResponseDto;
import com.backend.babyspa.v1.dtos.RegisterUserDto;
import com.backend.babyspa.v1.models.Role;
import com.backend.babyspa.v1.models.User;
import com.backend.babyspa.v1.models.UserRole;
import com.backend.babyspa.v1.models.UserRoleKey;
import com.backend.babyspa.v1.repositories.RoleRepository;
import com.backend.babyspa.v1.repositories.UserRepository;
import com.backend.babyspa.v1.repositories.UserRoleRepository;
import com.backend.babyspa.v1.utils.JwtUtil;

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
	RoleRepository roleRepository;

	public User register(RegisterUserDto registerUserDto) {

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

		userRepository.save(user);

		// kreiramo prvog usera i njemu dodijeljujemo rolu super_admin
		if (userRepository.count() == 1) {
			Role role = roleRepository.findByRoleName("ROLE_SUPER_ADMIN");
			UserRole userRole = new UserRole();
			userRole.setRole(role);
			userRole.setUser(user);
			userRole.setUserRoleKey(new UserRoleKey(user.getUserId(), role.getRoleId()));
			userRoleRepository.save(userRole);
		}

		return user;
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

}
