package com.backend.babyspa.v1.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.babyspa.v1.dtos.ServicePackageDailyReportDto;
import com.backend.babyspa.v1.models.ReportSortEnum;
import com.backend.babyspa.v1.models.ServicePackageDailyReport;
import com.backend.babyspa.v1.projections.ServicePackagesDailyReportProjection;
import com.backend.babyspa.v1.repositories.ServicePackageDailyReportRepository;
import com.backend.babyspa.v1.utils.DateTimeUtil;

import jakarta.transaction.Transactional;

@Service
public class ServicePackageDailyReportService {

	@Autowired
	ServicePackageDailyReportRepository servicePackageDailyReportRepository;

	public ServicePackageDailyReport save(ServicePackageDailyReportDto servicePackageDailyReportDto) {

		ServicePackageDailyReport servicePackageDailyReport = new ServicePackageDailyReport();

		if (Objects.nonNull(servicePackageDailyReportDto.getServicePackage())) {
			servicePackageDailyReport.setServicePackage(servicePackageDailyReportDto.getServicePackage());
		}
		servicePackageDailyReport.setNumberOfUsedPackages(servicePackageDailyReportDto.getNumberOfUsedPackages());
		servicePackageDailyReport.setDate(servicePackageDailyReportDto.getDate());

		return servicePackageDailyReportRepository.save(servicePackageDailyReport);
	}

	public List<ServicePackagesDailyReportProjection> findAll(Integer servicePackageId, LocalDateTime startRangeDate,
			LocalDateTime endRangeDate, String groupDataType) {

		if (Objects.isNull(startRangeDate) && Objects.nonNull(endRangeDate)) {
			startRangeDate = DateTimeUtil.getDateTimeFromString("1999-01-01 00:00:00");
		} else if (Objects.nonNull(startRangeDate) && Objects.isNull(endRangeDate)) {
			endRangeDate = LocalDateTime.now().plusMinutes(15);
		}

		if (Objects.isNull(startRangeDate) && Objects.isNull(endRangeDate)) {
			if (groupDataType.equals(ReportSortEnum.day.name())) {
				return servicePackageDailyReportRepository.findAllByServicePackageId(servicePackageId);
			} else if (groupDataType.equals(ReportSortEnum.month.name())) {
				return servicePackageDailyReportRepository.findAllByServicePackageIdGroupByMonth(servicePackageId);
			} else if (groupDataType.equals(ReportSortEnum.year.name())) {
				return servicePackageDailyReportRepository.findAllByServicePackageIdGroupByYear(servicePackageId);
			}

		} else {
			if (groupDataType.equals(ReportSortEnum.day.name())) {
				return servicePackageDailyReportRepository.findAllByServicePackageIdAndStartDateAndEndDate(
						servicePackageId, startRangeDate.toLocalDate(), endRangeDate.toLocalDate());
			} else if (groupDataType.equals(ReportSortEnum.month.name())) {
				return servicePackageDailyReportRepository.findAllByServicePackageIdAndStartDateAndEndDateGroupByMonth(
						servicePackageId, startRangeDate.toLocalDate(), endRangeDate.toLocalDate());
			} else if (groupDataType.equals(ReportSortEnum.year.name())) {
				return servicePackageDailyReportRepository.findAllByServicePackageIdAndStartDateAndEndDateGroupByYear(
						servicePackageId, startRangeDate.toLocalDate(), endRangeDate.toLocalDate());
			}
		}
		return null;
	}

	public boolean existsByDate(LocalDate date) {

		return servicePackageDailyReportRepository.existsByDate(date);
	}

	@Transactional
	public void deleteAll() {

		servicePackageDailyReportRepository.deleteAll();
	}

	@Transactional
	public void deleteByDate(LocalDate date) {

		servicePackageDailyReportRepository.deleteByDate(date);
	}

}
