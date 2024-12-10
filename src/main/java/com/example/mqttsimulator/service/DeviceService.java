package com.example.mqttsimulator.service;

import com.example.mqttsimulator.model.Device;
import com.example.mqttsimulator.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private static final int BATCH_SIZE = 1000; // Adjust batch size as needed

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MessageChannel mqttOutboundChannel;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Scheduled(fixedDelayString = "${device.simulation.interval:5000}")
    public void simulateDevices() {
        logger.info("#### Starting device simulation ####");
        long startTime = System.currentTimeMillis();
        int totalDevices = 0;
        int totalBatches = 0;

        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
        Page<Device> devicePage;

        do {
            devicePage = deviceRepository.findAll(pageable);
            List<Device> devices = devicePage.getContent();
            totalDevices += devices.size();
            totalBatches++;
            devices.forEach(device -> executorService.submit(() -> sendDeviceMessage(device)));
            pageNumber++;
            pageable = PageRequest.of(pageNumber, BATCH_SIZE);
        } while (devicePage.hasNext());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        logger.info("#### Device simulation completed ####");
        logger.info("Total devices processed: {}", totalDevices);
        logger.info("Total batches processed: {}", totalBatches);
        logger.info("Total time taken: {} ms", duration);
    }

    private void sendDeviceMessage(Device device) {
        logger.info("Sending message for device: {}", device.getName());
        String message = device.getMessage();
        if ("random".equalsIgnoreCase(message)) {
            message = String.valueOf(new Random().nextInt(100));
        }
        Message<String> mqttMessage = MessageBuilder.withPayload(message)
                .setHeader(MqttHeaders.TOPIC, device.getTopic())
                .build();
        mqttOutboundChannel.send(mqttMessage);
        logger.info("Sent message '{}' to topic '{}'", message, device.getTopic());
    }

    @Transactional
    public void createDevices(List<Device> devices) {
        logger.info("Starting batch device creation");
        long startTime = System.currentTimeMillis();
        int totalDevices = devices.size();
        int totalBatches = (int) Math.ceil((double) totalDevices / BATCH_SIZE);

        for (int i = 0; i < totalBatches; i++) {
            int start = i * BATCH_SIZE;
            int end = Math.min(start + BATCH_SIZE, totalDevices);
            List<Device> batch = devices.subList(start, end);
            deviceRepository.saveAll(batch);
            logger.info("Batch {} of {} processed", i + 1, totalBatches);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        logger.info("Batch device creation completed");
        logger.info("Total devices created: {}", totalDevices);
        logger.info("Total batches processed: {}", totalBatches);
        logger.info("Total time taken: {} ms", duration);
    }
}