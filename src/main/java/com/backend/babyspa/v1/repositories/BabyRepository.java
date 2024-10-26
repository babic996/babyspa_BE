package com.backend.babyspa.v1.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.babyspa.v1.models.Baby;

@Repository
public interface BabyRepository extends JpaRepository<Baby, Integer> {

	boolean existsByPhoneNumberAndBabyName(String phoneNumber, String babyName);

	boolean existsByPhoneNumberAndBabyNameAndBabyIdNot(String phoneNumber, String babyName, int babyId);

	@Query(value = """
			    SELECT *
			    FROM baby
			    WHERE (:searchText IS NULL
			           OR LOWER(baby_name) LIKE LOWER(CONCAT('%', :searchText, '%'))
			           OR LOWER(baby_surname) LIKE LOWER(CONCAT('%', :searchText, '%'))
			           OR REPLACE(phone_number, '+', '') LIKE CONCAT('%', REPLACE(:searchText, '+', ''), '%'))
			      AND (birth_date >= :startDate AND birth_date <= :endDate)
			    ORDER BY baby_id DESC
			""", nativeQuery = true)
	Page<Baby> findAllNativeWithDate(@Param("searchText") String searchText,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

	@Query(value = """
			    SELECT *
			    FROM baby
			    WHERE (:searchText IS NULL
			           OR LOWER(baby_name) LIKE LOWER(CONCAT('%', :searchText, '%'))
			           OR LOWER(baby_surname) LIKE LOWER(CONCAT('%', :searchText, '%'))
			           OR REPLACE(phone_number, '+', '') LIKE CONCAT('%', REPLACE(:searchText, '+', ''), '%'))
			    ORDER BY baby_id DESC
			""", nativeQuery = true)
	Page<Baby> findAllNativeWithoutDate(@Param("searchText") String searchText, Pageable pageable);

}
