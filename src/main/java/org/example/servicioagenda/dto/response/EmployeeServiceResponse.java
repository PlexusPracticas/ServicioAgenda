package org.example.servicioagenda.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmployeeServiceResponse {
    private List<EmployeeExternalDTO> employees;
    private int totalPages;
    private long totalElements;
}
