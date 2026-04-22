package org.example.servicioagenda.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignedDeviceInputDTO {

    private String serialNumber;
    private Integer assignedTo;

    //solo para creación
    private String brand;
    private String model;
    private String operatingSystem;

}
