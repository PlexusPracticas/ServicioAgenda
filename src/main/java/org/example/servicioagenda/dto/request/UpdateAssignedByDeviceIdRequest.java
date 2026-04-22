package org.example.servicioagenda.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAssignedByDeviceIdRequest {

    private Integer deviceId;
    private Integer assignedTo;
}

