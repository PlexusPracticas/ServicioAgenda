package org.example.servicioagenda.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeviceAdd {
    private String serialNumber;
    private String brand;
    private String model;
    private String operatingSystem;
    private Integer assignedTo;
}
