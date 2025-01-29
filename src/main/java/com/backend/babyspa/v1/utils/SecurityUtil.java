package com.backend.babyspa.v1.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.backend.babyspa.v1.models.User;

public class SecurityUtil {

	public static User getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			return (User) principal;
		}

		return null;
	}
}
