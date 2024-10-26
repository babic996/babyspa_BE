package com.backend.babyspa.v1.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.babyspa.v1.dtos.CreateReservationDto;
import com.backend.babyspa.v1.dtos.ReservationDailyReportDto;
import com.backend.babyspa.v1.dtos.ReservationFindAllDto;
import com.backend.babyspa.v1.dtos.ServicePackageDailyReportDto;
import com.backend.babyspa.v1.dtos.UpdateReservationDto;
import com.backend.babyspa.v1.exceptions.NotFoundException;
import com.backend.babyspa.v1.models.Arrangement;
import com.backend.babyspa.v1.models.Reservation;
import com.backend.babyspa.v1.models.ServicePackage;
import com.backend.babyspa.v1.models.Status;
import com.backend.babyspa.v1.repositories.ReservationRepository;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {

	@Autowired
	ReservationRepository reservationRepository;

	@Autowired
	ArrangementService arrangementService;

	@Autowired
	StatusService statusService;

	@Autowired
	ReservationDailyReportService reservationDailyReportService;

	@Autowired
	ServicePackageService servicePackageService;

	@Autowired
	ServicePackageDailyReportService servicePackageDailyReportService;

	private final String reservationReserved = "term_reserved";
	private final String reservationCanceled = "term_canceled";
	private final String reservationStatusType = "reservation";

	public Reservation findById(int reservationId) throws NotFoundException {

		return reservationRepository.findById(reservationId)
				.orElseThrow(() -> new NotFoundException("Nije pronadjena rezervacija sa id: " + reservationId + "!"));
	}

	@Transactional
	public ReservationFindAllDto save(CreateReservationDto createReservationDto) throws NotFoundException, Exception {

		Arrangement arrangement = arrangementService.findById(createReservationDto.getArrangementId());
		Status status = statusService.findByStatusCode(reservationReserved);
		Reservation reservation = new Reservation();

		if (arrangement.getRemainingTerm() == 0) {
			throw new Exception("Nije moguće napraviti rezervaciju jer je iskorišten maksimalan broj termina!");
		}

		if (reservationRepository.existsByArrangement(arrangement) && (createReservationDto.getStartDate()
				.plusDays(arrangement.getServicePackage().getServicePackageDurationDays())
				.isBefore(createReservationDto.getStartDate()))) {
			throw new Exception("Nije moguće napraviti rezervaciju jer je broj dana koliko traje paket istekao!");
		}

		reservation.setArrangement(arrangement);
		reservation.setStartDate(createReservationDto.getStartDate());
		reservation.setEndDate(
				createReservationDto.getStartDate().plusMinutes(createReservationDto.getDurationReservation()));
		reservation.setStatus(status);
		reservation.setNote(createReservationDto.getNote());
		arrangementService.decreaseRemainingTerm(arrangement);

		reservationRepository.save(reservation);

		return buildReservationFindAllDtoFromReservation(reservation);
	}

	public ReservationFindAllDto update(UpdateReservationDto updateReservationDto) throws NotFoundException, Exception {

		Status status = statusService.findById(updateReservationDto.getStatusId());
		Reservation reservation = findById(updateReservationDto.getReservationId());

		if (!reservation.getStatus().getStatusCode().equals(reservationCanceled)  && status.getStatusCode().equals(reservationCanceled)) {
			arrangementService.increaseRemainingTerm(reservation.getArrangement());
		} 
		
		if(reservation.getStatus().getStatusCode().equals(reservationCanceled) && !status.getStatusCode().equals(reservationCanceled)) {
			arrangementService.decreaseRemainingTerm(reservation.getArrangement());
		}

		reservation.setStatus(status);
		reservation.setNote(updateReservationDto.getNote());

		reservationRepository.save(reservation);

		return buildReservationFindAllDtoFromReservation(reservation);
	}

	@Transactional
	public int delete(int reservationId) throws NotFoundException {

		Reservation reservation = findById(reservationId);

		arrangementService.increaseRemainingTerm(reservation.getArrangement());
		reservationRepository.delete(reservation);

		return reservationId;
	}

	public int reservationCanceled(int reservationId) throws NotFoundException {

		Reservation reservation = findById(reservationId);
		Status status = statusService.findByStatusCode(reservationCanceled);

		arrangementService.increaseRemainingTerm(reservation.getArrangement());
		reservation.setStatus(status);
		reservationRepository.save(reservation);

		return reservationId;
	}

	public List<ReservationFindAllDto> findAllList() {

		return reservationRepository.findAll().stream().map(x -> buildReservationFindAllDtoFromReservation(x))
				.collect(Collectors.toList());
	}

	public List<Reservation> findAllByArrangementId(int arrangementId) {

		Arrangement arrangement = arrangementService.findById(arrangementId);

		return reservationRepository.findByArrangement(arrangement);
	}

	public void generateReservationReport(LocalDate date) {
		List<Status> statuses = statusService.findAllByStatusTypeCode(reservationStatusType);

		if (!statuses.isEmpty()) {
			statuses.forEach(status -> {
				List<Object[]> usegesPerBaby = reservationRepository.countReservationPerBabyAndStatus(date,
						status.getStatusId());
				if (Objects.nonNull(usegesPerBaby)) {
					usegesPerBaby.forEach(useges -> {
						ReservationDailyReportDto reservationDailyReportDto = new ReservationDailyReportDto();
						reservationDailyReportDto.setDate(date);
						reservationDailyReportDto.setBabyId(((Number) useges[1]).intValue());
						reservationDailyReportDto.setNumberOfReservation(((Number) useges[0]).intValue());
						reservationDailyReportDto.setStatus(status);
						reservationDailyReportService.save(reservationDailyReportDto);
					});
				}
			});
		}

	}

	public void generateServicePackageReport(LocalDate date) {
		List<ServicePackage> servicePackages = servicePackageService.findAll();

		if (!servicePackages.isEmpty()) {
			servicePackages.forEach(servicePackage -> {
				ServicePackageDailyReportDto servicePackageDailyReportDto = new ServicePackageDailyReportDto();
				int usedPackages = reservationRepository.countServicePackageByStartDateAndServicePackageId(date,
						servicePackage.getServicePackageId());
				servicePackageDailyReportDto.setNumberOfUsedPackages(usedPackages);
				servicePackageDailyReportDto.setDate(date);
				servicePackageDailyReportDto.setServicePackage(servicePackage);
				servicePackageDailyReportService.save(servicePackageDailyReportDto);
			});
		}
	}

	private ReservationFindAllDto buildReservationFindAllDtoFromReservation(Reservation reservation) {

		ReservationFindAllDto reservationFindAllDto = new ReservationFindAllDto();

		reservationFindAllDto.setReservationId(reservation.getReservationId());
		reservationFindAllDto.setStatus(reservation.getStatus());
		reservationFindAllDto.setEndDate(reservation.getEndDate());
		reservationFindAllDto.setStartDate(reservation.getStartDate());
		reservationFindAllDto.setCreatedAt(reservation.getCreatedAt());
		reservationFindAllDto.setNote(reservation.getNote());
		reservationFindAllDto.setArrangement(
				arrangementService.buildFindAllArrangementDtoFromArrangement(reservation.getArrangement()));

		return reservationFindAllDto;
	}

	public boolean existingByArrangement(int arrangementId) {

		Arrangement arrangement = arrangementService.findById(arrangementId);

		return reservationRepository.existsByArrangement(arrangement);
	}

//	@Async
	public void generateReportForAllDateInReservation(boolean generateForAllDays, LocalDate date) {

		if (generateForAllDays) {
			reservationDailyReportService.deleteAll();
			servicePackageDailyReportService.deleteAll();

			LocalDate currentDate = LocalDate.now();
			List<LocalDate> allDatesFromReservation = reservationRepository
					.findDistinctReservationDates(currentDate.atStartOfDay()).stream().map(x -> x.getDate())
					.collect(Collectors.toList());

			allDatesFromReservation.forEach(x -> {
				generateReservationReport(x);
				generateServicePackageReport(x);
			});
		} else {
			if (reservationDailyReportService.existsByDate(date)) {
				reservationDailyReportService.deleteByDate(date);
			}
			if (servicePackageDailyReportService.existsByDate(date)) {
				servicePackageDailyReportService.deleteByDate(date);
			}

			generateServicePackageReport(date);
			generateReservationReport(date);
		}
	}

	@Transactional
	public void deleteByArrangement(Arrangement arrangement) {

		reservationRepository.deleteByArrangement(arrangement);
	}

}
