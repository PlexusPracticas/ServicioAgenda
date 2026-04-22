package org.example.servicioagenda.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateAssignedToRequest {
    private List<AssignedDeviceInputDTO> devices;
    private String serialNumber;
    private Integer assignedTo;

}

