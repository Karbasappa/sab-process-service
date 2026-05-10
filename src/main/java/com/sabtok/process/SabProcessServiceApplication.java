package com.sabtok.process;

import com.sabtok.process.entity.AlertMessage;
import com.sabtok.process.repository.AlertMessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
@EnableFeignClients(basePackages = "com.sabtok.process.openfeign")
@EnableCaching
public class SabProcessServiceApplication implements CommandLineRunner {

	@Autowired
	private  AlertMessageRepo alertMessageRepo;

	@GetMapping("/status")
	public String getStatus() {
		return "Service is up and running";
	}

	public static void main(String[] args) {
		SpringApplication.run(SabProcessServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		AlertMessage message1 = new AlertMessage("CPU usage is high", "HIGH");
		AlertMessage message2 = new AlertMessage("Memory usage is high", "MEDIUM");
		AlertMessage message3 = new AlertMessage("Disk space is low", "LOW");
		List<AlertMessage> messageList = List.of(message1, message2, message3);
		alertMessageRepo.saveAll(messageList);
		System.out.println("Sample alert messages saved to the database");
	}
}
