package com.backend.babyspa.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.babyspa.v1.dtos.AssignRolesDto;
import com.backend.babyspa.v1.dtos.ChangePasswordDto;
import com.backend.babyspa.v1.dtos.LoginDto;
import com.backend.babyspa.v1.dtos.LoginResponseDto;
import com.backend.babyspa.v1.dtos.RegisterUserDto;
import com.backend.babyspa.v1.models.User;
import com.backend.babyspa.v1.services.UserService;
import com.backend.babyspa.v1.utils.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

	@Autowired
	UserService userService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterUserDto registerUserDto,
			Authentication authentication, BindingResult bindingResult) {

		if (hasErrors(bindingResult)) {
			return createErrorResponse(bindingResult);
		}

		try {
			User registerUser = userService.register(registerUserDto, authentication);
			return createSuccessResponse(registerUser);
		} catch (Exception e) {
			return createExceptionResponse(e);
		}

	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginDto loginDto,
			BindingResult bindingResult) {

		if (hasErrors(bindingResult)) {
			return createErrorResponse(bindingResult);
		}

		try {
			LoginResponseDto loginResponseDto = userService.loginUser(loginDto);
			return createSuccessResponse(loginResponseDto);
		} catch (Exception e) {
			return createExceptionResponse(e);
		}

	}

	@PutMapping("/change-password")
	public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordDto changePasswordDto,
			Authentication authentication) {

		try {
			return createSuccessResponse(userService.changePassword(changePasswordDto, authentication));
		} catch (Exception e) {
			return createExceptionResponse(e);
		}
	}

	@PutMapping("/assign-roles")
	public ResponseEntity<ApiResponse<String>> assignRoles(@RequestBody AssignRolesDto assignRolesDto,
			Authentication authentication) {

		try {
			return createSuccessResponse(userService.assignRolesToUser(assignRolesDto, authentication));
		} catch (Exception e) {
			return createExceptionResponse(e);
		}
	}
}
