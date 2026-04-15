package org.example.servicioagenda.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmployeeExternalDTO {
    private Integer id;
    private String name;
    private String surname;
    private String mailPlexus;
    private String clientId;
    private String mailClient;
    private String phoneNumber;
    private String phoneSn;
}
