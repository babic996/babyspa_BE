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
import com.backend.babyspa.v1.models.User;
import com.backend.babyspa.v1.repositories.UserRepository;
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

	public User register(RegisterUserDto registerUserDto) {

		User user = new User();

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

}
