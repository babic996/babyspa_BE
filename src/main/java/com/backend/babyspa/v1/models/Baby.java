package com.backend.babyspa.v1.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "baby")
public class Baby {

	@Id
	@Column(name = "baby_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int babyId;

	@Column(name = "baby_name", nullable = false)
	private String babyName;

	@Column(name = "baby_surname", nullable = true)
	private String babySurname;

	@Column(name = "number_of_months", nullable = false)
	private int numberOfMonths;

	@Column(name = "birth_date", nullable = true)
	private LocalDateTime birthDate;

	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;

	@Column(name = "mother_name", nullable = true)
	private String motherName;

	@Column(name = "note", nullable = true)
	private String note;
}
