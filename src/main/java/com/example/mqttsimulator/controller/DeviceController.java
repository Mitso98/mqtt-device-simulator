package com.example.mqttsimulator.controller;

import com.example.mqttsimulator.model.Device;
import com.example.mqttsimulator.repository.DeviceRepository;
import com.example.mqttsimulator.service.DeviceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        return deviceRepository.save(device);
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> createDevices(@RequestBody List<Device> devices) {
        deviceService.createDevices(devices);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> createTestDevices(@RequestParam int count) {
        List<Device> devices = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setTopic("device/1"); // TODO: control whther all devices in one topic or different topics
            device.setMessage("random");
            device.setFrequency(5);
            devices.add(device);
        }
        deviceService.createDevices(devices);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public Device updateDevice(@PathVariable Long id, @RequestBody Device deviceDetails) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setName(deviceDetails.getName());
        device.setTopic(deviceDetails.getTopic());
        device.setMessage(deviceDetails.getMessage());
        device.setFrequency(deviceDetails.getFrequency());
        return deviceRepository.save(device);
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        deviceRepository.deleteById(id);
    }
}