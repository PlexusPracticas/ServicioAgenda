package org.example.servicioagenda.model;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class Employee {
    private Integer employeeId;
    private String name;
    private String surname;
    private String plexusMail;
    private String mailClient;
    private String phoneNumber;
    private String clientId;
    private Device assignedDevice;


    public Employee() {
    }

}
