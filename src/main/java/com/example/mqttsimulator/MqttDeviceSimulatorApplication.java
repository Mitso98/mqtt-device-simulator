package com.example.mqttsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MqttDeviceSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqttDeviceSimulatorApplication.class, args);
    }
}