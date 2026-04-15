package org.example.servicioagenda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorListResponse {
    private List<ContactErrorDto> contactErrorDtos;
    private List<ErrorDTO> error;


    public ErrorListResponse(List<ContactErrorDto> contactErrors) {
        this.contactErrorDtos = contactErrors;
    }
    public ErrorListResponse(List<ErrorDTO> generalErrors, boolean isGeneral) {
        this.error = generalErrors;
    }

}
