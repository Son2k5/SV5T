package com.example.SinhVien5T;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;

@SpringBootTest
class SinhVien5TApplicationTests {

	@Autowired
	private com.example.SinhVien5T.campaign.repository.StandardRepository standardRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void printStandards() {
		System.out.println("========== STANDARDS DB INSPECTION ==========");
		standardRepository.findAll().forEach(s -> {
			System.out.printf("ID: %d | Name: %s | Level: %s | CampaignId: %s | PublicId: %s\n",
				s.getId(), s.getName(), s.getLevel(), s.getCampaign() != null ? s.getCampaign().getId() : "null", s.getPublicId());
		});
		System.out.println("=============================================");
	}

}

