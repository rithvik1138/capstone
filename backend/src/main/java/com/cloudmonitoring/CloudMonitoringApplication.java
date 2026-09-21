package com.cloudmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cloud Infrastructure Monitoring and Logging System - Main Application Entry Point.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CloudMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudMonitoringApplication.class, args);
    }
}