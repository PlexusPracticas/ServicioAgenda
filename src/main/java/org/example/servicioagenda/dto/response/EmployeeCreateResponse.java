package org.example.servicioagenda.dto.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCreateResponse {
    private Integer id;
    private String name;
    private String surname;
    private String plexusMail;
    private String mailClient;
    private String phoneNumber;
    private String phoneSn;
}

