package org.example.servicioagenda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDTO {
    private Integer deviceId;
    private String serialNumber;
    private String brand;
    private String model;
    private String operatingSystem;
    private Integer assignedTo;



}
