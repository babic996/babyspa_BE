package com.backend.babyspa.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDto {

	private String username;
	private String email;
	private String password;
	private String firstName;
	private String lastName;
}
