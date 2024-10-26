package com.backend.babyspa.v1.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.babyspa.v1.models.Arrangement;
import com.backend.babyspa.v1.models.Reservation;
import com.backend.babyspa.v1.projections.LocalDateProjection;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

	boolean existsByArrangement(Arrangement arrangement);

	List<Reservation> findByArrangement(Arrangement arrangement);

	void deleteByArrangement(Arrangement arrangement);

	@Query(value = "SELECT COUNT(r) FROM reservation r WHERE DATE(r.start_date) = :currentDate AND r.status_id = :statusId", nativeQuery = true)
	int countReservationByStartDateAndStatusId(@Param("currentDate") LocalDate currentDate,
			@Param("statusId") int statusId);

	@Query(value = """
			SELECT
			COUNT(r) AS reservation_count,
			a.baby_id
			FROM
			reservation r
			JOIN
			arrangement a ON r.arrangement_id = a.arrangement_id
			WHERE
			DATE(r.start_date) = :currentDate AND r.status_id = :statusId
			GROUP BY
			 a.baby_id;
			""", nativeQuery = true)
	List<Object[]> countReservationPerBabyAndStatus(@Param("currentDate") LocalDate currentDate,
			@Param("statusId") int statusId);

	@Query(value = """
			SELECT COALESCE(SUM(counts), 0) AS total_count
			FROM (
			SELECT COUNT(r.*) AS counts
			FROM reservation r
			LEFT JOIN arrangement a ON r.arrangement_id = a.arrangement_id
			WHERE DATE(r.start_date) = :currentDate AND a.service_package_id = :servicePackageId
			GROUP BY r.arrangement_id
			) AS subquery
			""", nativeQuery = true)
	int countServicePackageByStartDateAndServicePackageId(@Param("currentDate") LocalDate currentDate,
			@Param("servicePackageId") int servicePackageId);

	@Query(value = "SELECT DISTINCT DATE(start_date) FROM reservation WHERE start_date < :currentDateTime", nativeQuery = true)
	List<LocalDateProjection> findDistinctReservationDates(LocalDateTime currentDateTime);

}
