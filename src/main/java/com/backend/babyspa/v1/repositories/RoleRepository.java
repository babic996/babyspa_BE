package com.backend.babyspa.v1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.babyspa.v1.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

	Role findByRoleName(String name);
}
