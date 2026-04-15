package org.example.servicioagenda.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.servicioagenda.model.Device;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeInputDTO {
    private Integer employeeId; // Solo se usa en la funcionalidad de modificado
    private String name;
    private String surname;
    private String mailPlexus;
    private String mailClient;
    private String phoneNumber;
    private String clientId; //se usa solo en la funcionaldiad de modificado
    private AssignedDeviceInputDTO assignedDevice; //se usa solo en el AgregarContacto
    private Boolean deleteAssignedDevice; // Solo se usa en la funcionalidad de modificado
    private AssignedDeviceInputDTO addDevice; // Solo se usa en la funcionalidad de modificado
    private Boolean deleteEmployee; // Solo se usa en la funcionalidad de modificado

    public EmployeeInputDTO(){
    }


}
