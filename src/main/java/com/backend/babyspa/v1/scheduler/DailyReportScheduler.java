package com.backend.babyspa.v1.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.babyspa.v1.services.ReservationService;

@Component
public class DailyReportScheduler {

	@Autowired
	ReservationService reservationService;

	@Scheduled(cron = "00 59 23 * * *", zone = "Europe/Belgrade")
	public void generateReports() {
//		da se aplikacija vrti na serveru ovako bih generisao izvjestaje
// 		generisao bih svaki dan izvjestaj za taj dan
//		reservationService.generateReservationReport();
//		reservationService.generateServicePackageReport();
	}

	@Scheduled(cron = "0 0 8,9,10,11,12 * * *", zone = "Europe/Belgrade")
	public void updateReservationStatusDayBefore() {
		reservationService.updateReservationWithStatusCreatedToStatusUsed();
	}

}
