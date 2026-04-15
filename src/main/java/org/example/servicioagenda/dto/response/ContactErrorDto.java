package org.example.servicioagenda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@JsonInclude(JsonInclude.Include.NON_NULL)

public class ContactErrorDto {

    private String employeeId;
    private String message;
    public ContactErrorDto(String employeeId, String message) {
        this.employeeId = employeeId;
        this.message = message;
    }

}
