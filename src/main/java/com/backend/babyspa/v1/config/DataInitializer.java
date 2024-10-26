package com.backend.babyspa.v1.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.backend.babyspa.v1.models.Discount;
import com.backend.babyspa.v1.models.PaymentType;
import com.backend.babyspa.v1.models.Status;
import com.backend.babyspa.v1.models.StatusType;
import com.backend.babyspa.v1.repositories.DiscountRepository;
import com.backend.babyspa.v1.repositories.PaymentTypeRepository;
import com.backend.babyspa.v1.repositories.StatusRepository;
import com.backend.babyspa.v1.repositories.StatusTypeRepository;

import jakarta.annotation.PostConstruct;


@Component
public class DataInitializer {
	
	@Autowired
	StatusRepository statusRepository;
	
	@Autowired
	StatusTypeRepository statusTypeRepository;
	
	@Autowired
	DiscountRepository discountRepository;
	
	@Autowired
	PaymentTypeRepository paymentTypeRepository;
	
	@PostConstruct
	public void init() {
		
		if(statusTypeRepository.count() == 0) {
			statusTypeRepository.save(new StatusType("reservation"));
			statusTypeRepository.save(new StatusType("arrangement"));
		}
		
		if(statusRepository.count() == 0) {
			statusRepository.save(new Status("Termin otkazan", "term_canceled", statusTypeRepository.findByStatusTypeCode("reservation")));
			statusRepository.save(new Status("Iskorišten termin", "term_used", statusTypeRepository.findByStatusTypeCode("reservation")));
			statusRepository.save(new Status("Termin nije iskorišten", "term_not_used", statusTypeRepository.findByStatusTypeCode("reservation")));
			statusRepository.save(new Status("Rezervisan termin", "term_reserved", statusTypeRepository.findByStatusTypeCode("reservation")));
			statusRepository.save(new Status("Plaćen", "paid", statusTypeRepository.findByStatusTypeCode("arrangement")));
			statusRepository.save(new Status("Nije plaćen", "not_paid", statusTypeRepository.findByStatusTypeCode("arrangement")));
			statusRepository.save(new Status("Kreiran", "created", statusTypeRepository.findByStatusTypeCode("arrangement")));
		}
		
		if(discountRepository.count() == 0) {
			discountRepository.save(new Discount(BigDecimal.valueOf(20), false, "20KM"));
			discountRepository.save(new Discount(BigDecimal.valueOf(30), false, "30KM"));
			discountRepository.save(new Discount(BigDecimal.valueOf(50), false, "50KM"));
			discountRepository.save(new Discount(BigDecimal.valueOf(20), true, "20%"));
			discountRepository.save(new Discount(BigDecimal.valueOf(30), true, "30%"));
			discountRepository.save(new Discount(BigDecimal.valueOf(50), true, "50%"));
		}
		
		if(paymentTypeRepository.count() == 0) {
			paymentTypeRepository.save(new PaymentType("Gotovinski", "cash"));
			paymentTypeRepository.save(new PaymentType("Poklon bon", "gift"));
		}
		
		
	}
}
