package org.example.servicioagenda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {
    private Integer id;
    private String message;

    public ErrorDTO(String message) {
        this.message = message;
    }

}
