package org.example.servicioagenda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.servicioagenda.model.Device;
import org.example.servicioagenda.model.Employee;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeAgendaDTO {
    private Integer employeeId;
    private String name;
    private String surname;
    private String mailPlexus;
    private String mailClient;
    private String phoneNumber;
    private String clientId;
    private DeviceDTO assignedDevice;

}
