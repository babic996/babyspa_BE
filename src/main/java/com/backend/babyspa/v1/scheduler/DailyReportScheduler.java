package com.backend.babyspa.v1.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.babyspa.v1.services.ReservationService;

@Component
public class DailyReportScheduler {

	@Autowired
	ReservationService reservationService;

	@Scheduled(cron = "00 59 23 * * *", zone = "Europe/Sarajevo")
	public void generateReports() {
		reservationService.generateReportForAllDateInReservation(true, null);
	}

	@Scheduled(cron = "0 0 1 * * *", zone = "Europe/Sarajevo")
	public void updateReservationStatusDayBefore() {
		reservationService.updateReservationWithStatusCreatedToStatusUsed();
	}

}
