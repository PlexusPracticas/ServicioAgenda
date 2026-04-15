package org.example.servicioagenda.model;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class Device {
    private Integer deviceId;
    private String brand;
    private String model;
    private String serialNumber;
    private String operatingSystem;

    public Device(){
    }

}
