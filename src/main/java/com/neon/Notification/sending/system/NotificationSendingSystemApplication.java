package com.neon.Notification.sending.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NotificationSendingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationSendingSystemApplication.class, args);
	}

}
