package org.example.servicioagenda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.servicioagenda.model.Employee;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgendaResponse {
    private List<EmployeeAgendaDTO> employee;
    private int totalPages;
    private long totalElements;

    public AgendaResponse(){
    }

}

