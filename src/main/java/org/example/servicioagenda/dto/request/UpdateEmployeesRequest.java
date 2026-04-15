package org.example.servicioagenda.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UpdateEmployeesRequest {
    private List<EmployeeInputDTO> employees;
}
