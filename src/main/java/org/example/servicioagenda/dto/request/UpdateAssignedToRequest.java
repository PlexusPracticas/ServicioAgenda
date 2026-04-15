package org.example.servicioagenda.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAssignedToRequest {

    private String serialNumber;   // número de serie del dispositivo
    private Integer assignedTo;    // id del empleado que se va a asignar

}

