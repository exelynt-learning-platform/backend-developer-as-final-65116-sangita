package com.exelent.booking;

import com.exelent.booking.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ResourceBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceBookingApplication.class, args);
    }
}
