package org.example.servicioagenda.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateEmployeesResult {
    private boolean anySuccess;
    private List<ContactErrorDto> errors = new ArrayList<>();

    public boolean isAnySuccess() {
        return anySuccess;
    }

    public void setAnySuccess(boolean anySuccess) {
        this.anySuccess = anySuccess;
    }

    public List<ContactErrorDto> getErrors() {
        return errors;
    }
}

